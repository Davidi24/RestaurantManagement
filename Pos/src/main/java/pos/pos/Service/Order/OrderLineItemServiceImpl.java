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
import pos.pos.Util.OrderFormater;

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
    private final OrderFormater orderFormater;

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
        OrderLineItem li = loadLineItemForOrder(orderId, dto.getId());

        String sanitizedNotes = dto.getNotes() != null
                ? orderFormater.sanitizeNotesOrderLine(dto.getNotes())
                : li.getNotes();

        boolean qtyChanged = !li.getQuantity().equals(dto.getQuantity());
        boolean notesChanged = !Objects.equals(li.getNotes(), sanitizedNotes);

        if (!qtyChanged && !notesChanged) {
            return lineItemMapper.toOrderLineItemResponse(li);
        }

        li.setQuantity(dto.getQuantity());
        if (dto.getNotes() != null) {
            li.setNotes(sanitizedNotes);
        }

        pricingService.priceLineItem(li);
        OrderLineItem saved = lineItemRepository.save(li);

        afterLineItemChange(
                order,
                OrderEventType.ITEM_UPDATED,
                userEmail,
                String.format("Updated %s to qty x%d", saved.getItemName(), saved.getQuantity())
        );

        return lineItemMapper.toOrderLineItemResponse(saved);
    }

    @Override
    public List<OrderLineItemResponseDTO> getLineItems(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId));
        return order.getLineItems().stream()
                .map(lineItemMapper::toOrderLineItemResponse)
                .toList();
    }

    @Override
    public OrderLineItemResponseDTO getLineItemById(Long orderId, Long lineItemId) {
        OrderLineItem li = loadLineItemForOrder(orderId, lineItemId);
        return lineItemMapper.toOrderLineItemResponse(li);
    }

    @Override
    @Transactional
    public void deleteLineItem(Long orderId, Long lineItemId, String userEmail) {
        Order order = loadMutableOrderLocked(orderId);
        OrderLineItem li = loadLineItemForOrder(orderId, lineItemId);

        order.getLineItems().remove(li);

        afterLineItemChange(
                order,
                OrderEventType.ITEM_DELETED,
                userEmail,
                String.format("Deleted item %s (id=%d)", li.getItemName(), li.getId())
        );
    }

    @Override
    @Transactional
    public OrderLineItemResponseDTO updateFulfillmentStatus(Long orderId, Long lineItemId, FulfillmentStatus status, String userEmail) {
        Order order = loadMutableOrderLocked(orderId);
        OrderLineItem li = loadLineItemForOrder(orderId, lineItemId);

        FulfillmentStatus from = li.getFulfillmentStatus();
        if (from != null && from.canTransitionTo(status)) {
            throw new InvalidOrderStateException("Illegal transition " + from + " -> " + status);
        }
        if (from == null && !(status == FulfillmentStatus.NEW || status == FulfillmentStatus.FIRED)) {
            throw new InvalidOrderStateException("Illegal transition null -> " + status);
        }

        li.setFulfillmentStatus(status);
        OrderLineItem saved = lineItemRepository.save(li);

        afterLineItemChange(
                order,
                OrderEventType.ITEM_UPDATED,
                userEmail,
                String.format("LineItem %d status %s", saved.getId(), status)
        );

        return lineItemMapper.toOrderLineItemResponse(saved);
    }

    /**
     * Ensures the line item has a concrete unit price, either from variant override or its own field.
     */
    private void ensureHasUnitPrice(OrderLineItem li) {
        if (unitPrice(li) == null) {
            throw new InvalidOrderStateException("Missing unit price for item");
        }
    }

    /**
     * Returns the effective unit price (variant override > item unit price) or null if not resolvable.
     */
    private Double unitPrice(OrderLineItem li) {
        var vs = li.getVariantSnapshot();
        if (vs != null && vs.getPriceOverride() != null) return vs.getPriceOverride();
        return li.getUnitPrice();
    }

    /**
     * Unit price as a short string (e.g., "7.50").
     */
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

    /**
     * Merges quantity into an existing line, re-prices, persists, updates totals, and logs.
     */
    private OrderLineItemResponseDTO mergeIntoExisting(
            Order order,
            OrderLineItem target,
            int qtyToAdd,
            String userEmail
    ) {
        target.setQuantity(target.getQuantity() + qtyToAdd);
        pricingService.priceLineItem(target);
        OrderLineItem saved = lineItemRepository.save(target);

        afterLineItemChange(
                order,
                OrderEventType.ITEM_UPDATED,
                userEmail,
                String.format("Merged %s: new qty x%d", saved.getItemName(), saved.getQuantity())
        );

        return lineItemMapper.toOrderLineItemResponse(saved);
    }

    /**
     * Adds a new line, prices it, persists, updates totals, and logs.
     */
    private OrderLineItemResponseDTO addAsNewLine(
            Order order,
            OrderLineItem li,
            String userEmail
    ) {
        li.setOrder(order);
        order.getLineItems().add(li);

        pricingService.priceLineItem(li);
        OrderLineItem saved = lineItemRepository.save(li);

        afterLineItemChange(
                order,
                OrderEventType.ITEM_ADDED,
                userEmail,
                String.format("Added %s x%d (unit %s)", saved.getItemName(), saved.getQuantity(), unitPriceStr(saved))
        );

        return lineItemMapper.toOrderLineItemResponse(saved);
    }

    /**
     * Recalculate totals and log in one place (consistent post-action).
     */
    private void afterLineItemChange(
            Order order,
            OrderEventType type,
            String userEmail,
            String meta
    ) {
        totalsService.recalculateTotals(order);
        orderEventService.logEvent(order, type, userEmail, meta);
    }

    /**
     * Takes an order id, locks and validates it is editable.
     * If status is CLOSED or VOIDED, edits are rejected.
     */
    private @NotNull Order loadMutableOrderLocked(Long orderId) {
        var order = orderRepository.findForUpdate(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId));
        if (order.getStatus() == OrderStatus.CLOSED || order.getStatus() == OrderStatus.VOIDED)
            throw new InvalidOrderStateException("Order not editable");
        return order;
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

    /**
     * Load a line item and ensure it belongs to the given order id.
     */
    private OrderLineItem loadLineItemForOrder(Long orderId, Long lineItemId) {
        OrderLineItem li = lineItemRepository.findById(lineItemId)
                .orElseThrow(() -> new OrderItemNotFound(orderId, lineItemId));
        if (!li.getOrder().getId().equals(orderId)) {
            throw new LineItemOrderMismatchException(li.getId(), orderId);
        }
        return li;
    }
}
