package pos.pos.DTO.Mapper;

import org.springframework.stereotype.Component;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemCreateDTO;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemResponseDTO;
import pos.pos.Entity.Order.FulfillmentStatus;
import pos.pos.Entity.Order.Order;
import pos.pos.Entity.Order.OrderLineItem;

@Component
public class OrderLineItemMapper {
    public OrderLineItem toOrderLineItem(OrderLineItemCreateDTO dto, Order order) {
        Integer qty = dto.getQuantity() != null ? dto.getQuantity() : 1;

        return OrderLineItem.builder()
                .order(order)
                .quantity(qty)
                .fulfillmentStatus(FulfillmentStatus.NEW)
                .lineDiscount(0.0)
                .build();
    }

    public OrderLineItemResponseDTO toOrderLineItemResponse(OrderLineItem entity) {
        return OrderLineItemResponseDTO.builder()
                .id(entity.getId())
                .menuItemId(entity.getMenuItemId())
                .itemName(entity.getItemName())
                .unitPrice(entity.getUnitPrice())
                .quantity(entity.getQuantity())
                .lineSubtotal(entity.getLineSubtotal())
                .lineDiscount(entity.getLineDiscount())
                .lineGrandTotal(entity.getLineGrandTotal())
                .build();
    }
}
