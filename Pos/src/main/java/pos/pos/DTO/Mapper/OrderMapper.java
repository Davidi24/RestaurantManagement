package pos.pos.DTO.Mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderCreateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderResponseDTO;
import pos.pos.Entity.Order.Order;

@Component
@RequiredArgsConstructor
public class OrderMapper {

    private final OrderLineItemMapper orderLineItemMapper;

    public Order toOrder(OrderCreateDTO dto) {
        return Order.builder()
                .staffId(dto.getStaffId())
                .tableId(dto.getTableId())
                .customerId(dto.getCustomerId())
                .notes(dto.getNotes())
                .status(pos.pos.Entity.Order.OrderStatus.OPEN)
                .type(pos.pos.Entity.Order.OrderType.DINE_IN)
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
                .staffId(order.getStaffId())
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
