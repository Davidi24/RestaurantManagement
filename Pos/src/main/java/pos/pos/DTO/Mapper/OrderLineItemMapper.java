package pos.pos.DTO.Mapper;

import org.springframework.stereotype.Component;
import pos.pos.DTO.Order.OrderLineItemDTO.*;
import pos.pos.Entity.Order.*;

@Component
public class OrderLineItemMapper {

    public OrderLineItem toOrderLineItem(OrderLineItemCreateDTO dto, Order order) {
        var li = OrderLineItem.builder()
                .menuItemId(dto.getMenuItemId())
                .itemName(dto.getItemName())
                .unitPrice(dto.getUnitPrice())
                .quantity(dto.getQuantity())
                .lineSubtotal(dto.getUnitPrice() * dto.getQuantity())
                .lineGrandTotal(dto.getUnitPrice() * dto.getQuantity())
                .order(order)
                .build();

        if (dto.getVariantSnapshot() != null) {
            var v = dto.getVariantSnapshot();
            var vs = OrderVariantSnapshot.builder()
                    .variantId(v.getVariantId())
                    .variantName(v.getVariantName())
                    .priceOverride(v.getPriceOverride())
                    .lineItem(li)
                    .build();
            li.setVariantSnapshot(vs);
        }

        if (dto.getOptionSnapshots() != null && !dto.getOptionSnapshots().isEmpty()) {
            var list = new java.util.ArrayList<OrderOptionSnapshot>();
            for (var o : dto.getOptionSnapshots()) {
                list.add(OrderOptionSnapshot.builder()
                        .optionId(o.getOptionId())
                        .optionName(o.getOptionName())
                        .priceDelta(o.getPriceDelta())
                        .lineItem(li)
                        .build());
            }
            li.setOptionSnapshots(list);
        }

        return li;
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
