package pos.pos.DTO.Mapper.OrderMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderCreateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderResponseDTO;
import pos.pos.Entity.Order.Order;
import pos.pos.Entity.Order.OrderStatus;
import pos.pos.Entity.Order.OrderType;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final OrderLineItemMapper orderLineItemMapper;

    public Order toOrder(OrderCreateDTO dto) {
        return Order.builder()
                .tableId(dto.getTableId())
                .status(OrderStatus.OPEN)
                .type(OrderType.DINE_IN)
                .build();
    }

    public OrderResponseDTO toOrderResponse(Order order) {
        return OrderResponseDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .type(order.getType().name())
                .openedAt(order.getOpenedAt())
                .closedAt(order.getClosedAt())
                .userEmail(order.getUserEmail())
                .tableId(order.getTableId())
                .customerId(order.getCustomerId())
                .notes(order.getNotes())
                .grandTotal(order.getTotals() != null ? order.getTotals().getGrandTotal() : null)
                .paidTotal(order.getTotals() != null ? order.getTotals().getPaidTotal() : null)
                .balanceDue(order.getTotals() != null ? order.getTotals().getBalanceDue() : null)
                .lineItems(order.getLineItems().stream()
                        .map(orderLineItemMapper::toOrderLineItemResponse)
                        .toList())
                .build();
    }
}
