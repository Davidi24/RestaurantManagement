package pos.pos.Service.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.OrderMapper.OrderLineItemMapper;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemCreateDTO;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemResponseDTO;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemUpdateDTO;
import pos.pos.Entity.Order.FulfillmentStatus;
import pos.pos.Entity.Order.Order;
import pos.pos.Entity.Order.OrderEventType;
import pos.pos.Entity.Order.OrderLineItem;
import pos.pos.Exeption.LineItemOrderMismatchException;
import pos.pos.Exeption.OrderItemNotFound;
import pos.pos.Exeption.OrderNotFound;
import pos.pos.Repository.Order.OrderLineItemRepository;
import pos.pos.Repository.Order.OrderRepository;
import pos.pos.Service.Interfecaes.OrderEventService;
import pos.pos.Service.Interfecaes.OrderLineItemService;
import pos.pos.Service.Interfecaes.TotalsService;

import java.util.List;

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
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFound(orderId));
        OrderLineItem lineItem = lineItemMapper.toOrderLineItem(dto, order);
        snapshotBuilder.enrichFromCatalog(lineItem, dto);
        pricingService.priceLineItem(lineItem);
        if (lineItem.getOrder() == null) lineItem.setOrder(order);
        if (!order.getLineItems().contains(lineItem)) order.getLineItems().add(lineItem);
        lineItem = lineItemRepository.save(lineItem);
        totalsService.recalculateTotals(order);
        String unitStr = (lineItem.getVariantSnapshot() != null
                && lineItem.getVariantSnapshot().getPriceOverride() != null)
                ? String.valueOf(lineItem.getVariantSnapshot().getPriceOverride())
                : String.valueOf(lineItem.getUnitPrice());
        String metadata = "Added " + lineItem.getItemName() + " x" + lineItem.getQuantity() + " (unit " + unitStr + ")";
        orderEventService.logEvent(order, OrderEventType.ITEM_ADDED, userEmail, metadata);
        return lineItemMapper.toOrderLineItemResponse(lineItem);
    }

    @Override
    public OrderLineItemResponseDTO updateLineItem(Long orderId, OrderLineItemUpdateDTO dto) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFound(orderId));
        OrderLineItem lineItem = lineItemRepository.findById(dto.getId()).orElseThrow(() -> new OrderItemNotFound(orderId, dto.getId()));
        if (!lineItem.getOrder().getId().equals(order.getId())) {
            throw new LineItemOrderMismatchException(lineItem.getId(), order.getId());
        }
        if (dto.getQuantity() != null) lineItem.setQuantity(dto.getQuantity());
        if (dto.getItemName() != null) lineItem.setItemName(dto.getItemName());
        pricingService.priceLineItem(lineItem);
        lineItem = lineItemRepository.save(lineItem);
        totalsService.recalculateTotals(order);
        String metadata = "Updated item " + lineItem.getItemName() + " to quantity " + lineItem.getQuantity();
        orderEventService.logEvent(order, OrderEventType.ITEM_UPDATED, null, metadata);
        return lineItemMapper.toOrderLineItemResponse(lineItem);
    }

    @Override
    public List<OrderLineItemResponseDTO> getLineItems(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFound(orderId));
        return order.getLineItems().stream().map(lineItemMapper::toOrderLineItemResponse).toList();
    }

    @Override
    public void deleteLineItem(Long orderId, Long lineItemId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFound(orderId));
        OrderLineItem lineItem = lineItemRepository.findById(lineItemId).orElseThrow(() -> new OrderItemNotFound(orderId, lineItemId));
        if (!lineItem.getOrder().getId().equals(orderId)) {
            throw new LineItemOrderMismatchException(lineItem.getId(), orderId);
        }
        order.getLineItems().remove(lineItem);
        lineItem.setOrder(null);
        lineItemRepository.delete(lineItem);
        totalsService.recalculateTotals(order);
        String metadata = "Deleted item " + lineItem.getItemName() + " (id=" + lineItem.getId() + ")";
        orderEventService.logEvent(order, OrderEventType.ITEM_DELETED, null, metadata);
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
    public OrderLineItemResponseDTO updateFulfillmentStatus(Long orderId, Long lineItemId, FulfillmentStatus status) {
        var order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFound(orderId));
        var li = lineItemRepository.findById(lineItemId).orElseThrow(() -> new OrderItemNotFound(orderId, lineItemId));
        if (!li.getOrder().getId().equals(orderId)) {
            throw new LineItemOrderMismatchException(li.getId(), orderId);
        }
        li.setFulfillmentStatus(status);
        li = lineItemRepository.save(li);
        orderEventService.logEvent(order, OrderEventType.ITEM_UPDATED, null, "LineItem " + li.getId() + " status " + status);
        return lineItemMapper.toOrderLineItemResponse(li);
    }



}
