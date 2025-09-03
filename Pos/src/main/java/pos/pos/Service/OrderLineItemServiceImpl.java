package pos.pos.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.OrderLineItemMapper;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemCreateDTO;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemResponseDTO;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemUpdateDTO;
import pos.pos.Entity.Order.Order;
import pos.pos.Entity.Order.OrderEventType;
import pos.pos.Entity.Order.OrderLineItem;
import pos.pos.Exeption.LineItemOrderMismatchException;
import pos.pos.Exeption.OrderItemNotFound;
import pos.pos.Exeption.OrderNotFound;
import pos.pos.Repository.OrderLineItemRepository;
import pos.pos.Repository.OrderRepository;
import pos.pos.Service.Interfecaes.OrderEventService;
import pos.pos.Service.Interfecaes.OrderLineItemService;

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

    @Override
    public OrderLineItemResponseDTO addLineItem(Long orderId, OrderLineItemCreateDTO dto, String userEmail) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId));

        OrderLineItem lineItem = lineItemMapper.toOrderLineItem(dto, order);
        snapshotBuilder.enrichFromCatalog(lineItem, dto);
        pricingService.priceLineItem(lineItem);

        lineItem = lineItemRepository.save(lineItem);

        String unitStr = (lineItem.getVariantSnapshot() != null
                && lineItem.getVariantSnapshot().getPriceOverride() != null)
                ? String.valueOf(lineItem.getVariantSnapshot().getPriceOverride())
                : String.valueOf(lineItem.getUnitPrice());

        String metadata = "Added " + lineItem.getItemName() + " x" + lineItem.getQuantity() +
                " (unit " + unitStr + ")";
        orderEventService.logEvent(order, OrderEventType.ITEM_ADDED, userEmail, metadata);

        return lineItemMapper.toOrderLineItemResponse(lineItem);
    }

    @Override
    public OrderLineItemResponseDTO updateLineItem(Long orderId, OrderLineItemUpdateDTO dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId));

        var lineItem = lineItemRepository.findById(dto.getId())
                .orElseThrow(() -> new OrderItemNotFound(orderId, dto.getId()));

        if (!lineItem.getOrder().getId().equals(order.getId())) {
            throw new LineItemOrderMismatchException(lineItem.getId(), order.getId());
        }

        lineItem.setQuantity(dto.getQuantity());
        lineItem.setItemName(dto.getItemName());
        lineItem.setLineSubtotal(lineItem.getUnitPrice() * lineItem.getQuantity());
        lineItem.setLineGrandTotal(lineItem.getUnitPrice() * lineItem.getQuantity());

        lineItem = lineItemRepository.save(lineItem);

        String metadata = "Updated item " + lineItem.getItemName() +
                " to quantity " + lineItem.getQuantity();
        orderEventService.logEvent(order, OrderEventType.ITEM_UPDATED, null, metadata);

        return lineItemMapper.toOrderLineItemResponse(lineItem);
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
    public void deleteLineItem(Long orderId, Long lineItemId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFound(orderId));

        var lineItem = lineItemRepository.findById(lineItemId)
                .orElseThrow(() -> new OrderItemNotFound(orderId, lineItemId));

        if (!lineItem.getOrder().getId().equals(orderId)) {
            throw new LineItemOrderMismatchException(lineItem.getId(), orderId);
        }

        lineItemRepository.delete(lineItem);

        String metadata = "Deleted item " + lineItem.getItemName() +
                " (id=" + lineItem.getId() + ")";
        orderEventService.logEvent(order, OrderEventType.ITEM_DELETED, null, metadata);
    }

    @Override
    public OrderLineItemResponseDTO getLineItemById(Long orderId, Long lineItemId) {
        var lineItem = lineItemRepository.findById(lineItemId)
                .orElseThrow(() -> new OrderItemNotFound(orderId, lineItemId));
        if (!lineItem.getOrder().getId().equals(orderId)) {
            throw new LineItemOrderMismatchException(lineItem.getId(), orderId);
        }

        return lineItemMapper.toOrderLineItemResponse(lineItem);
    }
}
