package pos.pos.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemCreateDTO;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemResponseDTO;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemUpdateDTO;
import pos.pos.Repository.OrderRepository;
import pos.pos.Repository.OrderLineItemRepository;
import pos.pos.DTO.Mapper.OrderLineItemMapper;
import pos.pos.Entity.Order.Order;
import pos.pos.Service.Interfecaes.OrderLineItemService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderLineItemServiceImpl implements OrderLineItemService {

    private final OrderRepository orderRepository;
    private final OrderLineItemRepository lineItemRepository;
    private final OrderLineItemMapper lineItemMapper;

    @Override
    public OrderLineItemResponseDTO addLineItem(Long orderId, OrderLineItemCreateDTO dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        var lineItem = lineItemMapper.toOrderLineItem(dto, order);
        lineItem = lineItemRepository.save(lineItem);
        return lineItemMapper.toOrderLineItemResponse(lineItem);
    }

    @Override
    public OrderLineItemResponseDTO updateLineItem(Long orderId, OrderLineItemUpdateDTO dto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        var lineItem = lineItemRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("LineItem not found"));
        if (!lineItem.getOrder().getId().equals(order.getId())) {
            throw new RuntimeException("LineItem does not belong to this order");
        }
        lineItem.setQuantity(dto.getQuantity());
        lineItem.setItemName(dto.getItemName());
        lineItem.setLineSubtotal(lineItem.getUnitPrice() * lineItem.getQuantity());
        lineItem.setLineGrandTotal(lineItem.getUnitPrice() * lineItem.getQuantity());
        lineItem = lineItemRepository.save(lineItem);
        return lineItemMapper.toOrderLineItemResponse(lineItem);
    }

    @Override
    public List<OrderLineItemResponseDTO> getLineItems(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return order.getLineItems().stream()
                .map(lineItemMapper::toOrderLineItemResponse)
                .toList();
    }

    @Override
    public void deleteLineItem(Long orderId, Long lineItemId) {
        var lineItem = lineItemRepository.findById(lineItemId)
                .orElseThrow(() -> new RuntimeException("LineItem not found"));
        if (!lineItem.getOrder().getId().equals(orderId)) {
            throw new RuntimeException("LineItem does not belong to this order");
        }
        lineItemRepository.delete(lineItem);
    }

    @Override
    public OrderLineItemResponseDTO getLineItemById(Long orderId, Long lineItemId) {
        var lineItem = lineItemRepository.findById(lineItemId)
                .orElseThrow(() -> new RuntimeException("LineItem not found"));
        if (!lineItem.getOrder().getId().equals(orderId)) {
            throw new RuntimeException("LineItem does not belong to this order");
        }
        return lineItemMapper.toOrderLineItemResponse(lineItem);
    }

}
