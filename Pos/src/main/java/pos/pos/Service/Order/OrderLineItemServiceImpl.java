package pos.pos.Service.Order;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.OrderMapper.OrderLineItemMapper;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemCreateDTO;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemResponseDTO;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemUpdateDTO;
import pos.pos.Entity.Order.*;
import pos.pos.Exeption.InvalidOrderStateException;
import pos.pos.Exeption.LineItemOrderMismatchException;
import pos.pos.Exeption.OrderItemNotFound;
import pos.pos.Exeption.OrderNotFound;
import pos.pos.Repository.Order.OrderLineItemRepository;
import pos.pos.Repository.Order.OrderRepository;
import pos.pos.Service.Interfecaes.Order.OrderEventService;
import pos.pos.Service.Interfecaes.Order.OrderLineItemService;
import pos.pos.Service.Interfecaes.Order.TotalsService;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderLineItemServiceImpl implements OrderLineItemService {

    private final OrderRepository orderRepository;
    private final OrderLineItemRepository lineItemRepository;
    private final OrderLineItemMapper lineItemMapper;
    private final OrderEventService orderEventService;
    private final OrderSnapshotBuilder snapshotBuilder;
    private final OrderPricingService pricingService;
    private final TotalsService totalsService;

    @Override
    public OrderLineItemResponseDTO addLineItem(Long orderId, OrderLineItemCreateDTO dto, String userEmail) {
        Order order = loadMutableOrderLocked(orderId);

        OrderLineItem incoming = lineItemMapper.toOrderLineItem(dto, order);
        snapshotBuilder.enrichFromCatalog(incoming, dto);
        ensureHasUnitPrice(incoming);

        OrderLineItem mergeTarget = findMergeTarget(order, incoming);

        if (mergeTarget != null) {
            return mergeIntoExisting(order, mergeTarget, incoming.getQuantity(), userEmail);
        }

        return addAsNewLine(order, incoming, userEmail);
    }

    @Override
    @Transactional
    public OrderLineItemResponseDTO updateLineItem(Long orderId, OrderLineItemUpdateDTO dto, String userEmail) {
        Order order = loadMutableOrderLocked(orderId);
        OrderLineItem lineItem = lineItemRepository.findById(dto.getId())
                .orElseThrow(() -> new OrderItemNotFound(orderId, dto.getId()));
        if (!lineItem.getOrder().getId().equals(order.getId())) {
            throw new LineItemOrderMismatchException(lineItem.getId(), order.getId());
        }
        lineItem.setQuantity(dto.getQuantity());
        if (dto.getItemName() != null && !dto.getItemName().isBlank()) {
            lineItem.setItemName(dto.getItemName().trim());
        }
        pricingService.priceLineItem(lineItem);
        totalsService.recalculateTotals(order);
        String metadata = "Updated item " + lineItem.getItemName() + " to quantity " + lineItem.getQuantity();
        orderEventService.logEvent(order, OrderEventType.ITEM_UPDATED, userEmail, metadata);
        return lineItemMapper.toOrderLineItemResponse(lineItem);
    }

    @Override
    public List<OrderLineItemResponseDTO> getLineItems(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFound(orderId));
        return order.getLineItems().stream().map(lineItemMapper::toOrderLineItemResponse).toList();
    }

    @Override
    @Transactional
    public void deleteLineItem(Long orderId, Long lineItemId, String userEmail) {
        Order order = loadMutableOrderLocked(orderId);
        OrderLineItem lineItem = lineItemRepository.findById(lineItemId)
                .orElseThrow(() -> new OrderItemNotFound(orderId, lineItemId));
        if (!lineItem.getOrder().getId().equals(orderId)) {
            throw new LineItemOrderMismatchException(lineItem.getId(), orderId);
        }
        order.getLineItems().remove(lineItem);
        totalsService.recalculateTotals(order);
        String metadata = "Deleted item " + lineItem.getItemName() + " (id=" + lineItem.getId() + ")";
        orderEventService.logEvent(order, OrderEventType.ITEM_DELETED, userEmail, metadata);
    }

    @Override
    public OrderLineItemResponseDTO getLineItemById(Long orderId, Long lineItemId) {
        OrderLineItem lineItem = lineItemRepository.findById(lineItemId).orElseThrow(() -> new OrderItemNotFound(orderId, lineItemId));
        if (!lineItem.getOrder().getId().equals(orderId)) {
            throw new LineItemOrderMismatchException(lineItem.getId(), orderId);
        }
        return lineItemMapper.toOrderLineItemResponse(lineItem);
    }

    @Override
    @Transactional
    public OrderLineItemResponseDTO updateFulfillmentStatus(Long orderId, Long lineItemId, FulfillmentStatus status, String userEmail) {
        Order order = loadMutableOrderLocked(orderId);
        OrderLineItem li = lineItemRepository.findById(lineItemId)
                .orElseThrow(() -> new OrderItemNotFound(orderId, lineItemId));
        if (!li.getOrder().getId().equals(orderId)) {
            throw new LineItemOrderMismatchException(li.getId(), orderId);
        }
        if (!isAllowedTransition(li.getFulfillmentStatus(), status)) {
            throw new InvalidOrderStateException("Illegal transition " + li.getFulfillmentStatus() + " -> " + status);
        }
        li.setFulfillmentStatus(status);
        orderEventService.logEvent(order, OrderEventType.ITEM_UPDATED, userEmail,
                "LineItem " + li.getId() + " status " + status);
        return lineItemMapper.toOrderLineItemResponse(li);
    }


// ─────────────────────────────────────────────────────────────────────────────
// Helpers
// ─────────────────────────────────────────────────────────────────────────────

    private static final String ERR_MISSING_UNIT_PRICE = "Missing unit price for item";

    /** Ensures the line item has a concrete unit price, either from variant override or its own field. */
    private void ensureHasUnitPrice(OrderLineItem li) {
        if (unitPrice(li) == null) {
            throw new InvalidOrderStateException(ERR_MISSING_UNIT_PRICE);
        }
    }

    /** Returns the effective unit price (variant override > item unit price) or null if not resolvable. */
    private Double unitPrice(OrderLineItem li) {
        var vs = li.getVariantSnapshot();
        if (vs != null && vs.getPriceOverride() != null) return vs.getPriceOverride();
        return li.getUnitPrice();
    }

    /** Unit price as a short string (e.g., "7.50"). */
    private String unitPriceStr(OrderLineItem li) {
        Double p = unitPrice(li);
        return (p == null) ? "0.00" : String.format("%.2f", p);
    }

    /**
     * Finds an existing line item in the order that matches the incoming item's spec
     * (same menu item / variant / options by public IDs).
     */
    private OrderLineItem findMergeTarget(Order order, OrderLineItem incoming) {
        return order.getLineItems().stream()
                .filter(existing -> sameSpecByPublicIds(existing, incoming))
                .findFirst()
                .orElse(null);
    }

    /** Merges quantity into an existing line, re-prices, persists, updates totals, and logs. */
    private OrderLineItemResponseDTO mergeIntoExisting(
            Order order,
            OrderLineItem target,
            int qtyToAdd,
            String userEmail
    ) {
        target.setQuantity(target.getQuantity() + qtyToAdd);
        pricingService.priceLineItem(target);        // also normalizes unitPrice
        lineItemRepository.save(target);
        totalsService.recalculateTotals(order);

        orderEventService.logEvent(
                order,
                OrderEventType.ITEM_UPDATED,
                userEmail,
                String.format("Merged %s: new qty x%d", target.getItemName(), target.getQuantity())
        );

        return lineItemMapper.toOrderLineItemResponse(target);
    }

    /** Adds a new line, prices it, persists, updates totals, and logs. */
    private OrderLineItemResponseDTO addAsNewLine(
            Order order,
            OrderLineItem li,
            String userEmail
    ) {
        li.setOrder(order);
        order.getLineItems().add(li);

        pricingService.priceLineItem(li);
        OrderLineItem saved = lineItemRepository.save(li);
        totalsService.recalculateTotals(order);

        orderEventService.logEvent(
                order,
                OrderEventType.ITEM_ADDED,
                userEmail,
                String.format("Added %s x%d (unit %s)", saved.getItemName(), saved.getQuantity(), unitPriceStr(saved))
        );

        return lineItemMapper.toOrderLineItemResponse(saved);
    }



    // This function takes an order id it checks if it exist if yes then check if you can edit it
    // byschecking the status. If it is an invalid status like CLOSED or VOIDED then you can not edit it
    private @NotNull Order loadMutableOrderLocked(Long orderId) {
        var order = orderRepository.findForUpdate(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId));
        if (order.getStatus() == OrderStatus.CLOSED || order.getStatus() == OrderStatus.VOIDED)
            throw new InvalidOrderStateException("Order not editable");
        return order;
    }


    private boolean isAllowedTransition(FulfillmentStatus from, FulfillmentStatus to) {
        if (from == null) return to == FulfillmentStatus.NEW || to == FulfillmentStatus.FIRED;
        return switch (from) {
            case NEW -> to == FulfillmentStatus.FIRED || to == FulfillmentStatus.VOIDED;
            case FIRED -> to == FulfillmentStatus.READY || to == FulfillmentStatus.VOIDED;
            case READY -> to == FulfillmentStatus.SERVED || to == FulfillmentStatus.VOIDED;
            case SERVED, VOIDED -> false;
        };
    }





    private boolean sameSpecByPublicIds(OrderLineItem a, OrderLineItem b) {
        var aVar = a.getVariantSnapshot() != null ? a.getVariantSnapshot().getVariantPublicId() : null;
        var bVar = b.getVariantSnapshot() != null ? b.getVariantSnapshot().getVariantPublicId() : null;

        var aOpts = a.getOptionSnapshots().stream()
                .map(OrderOptionSnapshot::getOptionPublicId).sorted().toList();
        var bOpts = b.getOptionSnapshots().stream()
                .map(OrderOptionSnapshot::getOptionPublicId).sorted().toList();

        return Objects.equals(a.getMenuItemId(), b.getMenuItemId())
                && Objects.equals(aVar, bVar)
                && aOpts.equals(bOpts);
    }


}