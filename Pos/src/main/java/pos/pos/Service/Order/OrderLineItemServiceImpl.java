package pos.pos.Service.Order;

import lombok.RequiredArgsConstructor;
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
import pos.pos.Service.Interfecaes.OrderEventService;
import pos.pos.Service.Interfecaes.OrderLineItemService;
import pos.pos.Service.Interfecaes.TotalsService;

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

        OrderLineItem lineItem = lineItemMapper.toOrderLineItem(dto, order);
        snapshotBuilder.enrichFromCatalog(lineItem, dto);

        if ((lineItem.getVariantSnapshot() == null || lineItem.getVariantSnapshot().getPriceOverride() == null)
                && lineItem.getUnitPrice() == null) {
            throw new InvalidOrderStateException("Missing unit price for item");
        }

        OrderLineItem incoming = lineItem;
        var existingLineOpt = order.getLineItems().stream()
                .filter(existingLine -> sameSpecByPublicIds(existingLine, incoming))
                .findFirst();

        if (existingLineOpt.isPresent()) {
            OrderLineItem existingLine = existingLineOpt.get();
            existingLine.setQuantity(existingLine.getQuantity() + incoming.getQuantity());
            pricingService.priceLineItem(existingLine);
            lineItemRepository.save(existingLine);
            totalsService.recalculateTotals(order);
            orderEventService.logEvent(order, OrderEventType.ITEM_UPDATED, userEmail,
                    "Merged qty for " + existingLine.getItemName() + " to x" + existingLine.getQuantity());
            return lineItemMapper.toOrderLineItemResponse(existingLine);
        }

        lineItem.setOrder(order);
        order.getLineItems().add(lineItem);
        pricingService.priceLineItem(lineItem);
        lineItem = lineItemRepository.save(lineItem);
        totalsService.recalculateTotals(order);

        String unitStr = (lineItem.getVariantSnapshot() != null && lineItem.getVariantSnapshot().getPriceOverride() != null)
                ? String.valueOf(lineItem.getVariantSnapshot().getPriceOverride())
                : String.valueOf(lineItem.getUnitPrice());
        orderEventService.logEvent(order, OrderEventType.ITEM_ADDED, userEmail,
                "Added " + lineItem.getItemName() + " x" + lineItem.getQuantity() + " (unit " + unitStr + ")");

        return lineItemMapper.toOrderLineItemResponse(lineItem);
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

    private boolean isAllowedTransition(FulfillmentStatus from, FulfillmentStatus to) {
        if (from == null) return to == FulfillmentStatus.NEW || to == FulfillmentStatus.FIRED;
        return switch (from) {
            case NEW -> to == FulfillmentStatus.FIRED || to == FulfillmentStatus.VOIDED;
            case FIRED -> to == FulfillmentStatus.READY || to == FulfillmentStatus.VOIDED;
            case READY -> to == FulfillmentStatus.SERVED || to == FulfillmentStatus.VOIDED;
            case SERVED, VOIDED -> false;
        };
    }

    private Order loadMutableOrderLocked(Long orderId){
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
}
