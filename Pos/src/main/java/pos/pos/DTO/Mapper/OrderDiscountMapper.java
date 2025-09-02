package pos.pos.DTO.Mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountCreateDTO;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountResponseDTO;
import pos.pos.Entity.Order.OrderDiscount;
import pos.pos.Entity.Order.OrderLineItem;

@Component
@RequiredArgsConstructor
public class OrderDiscountMapper {

    public OrderDiscount toOrderDiscount(OrderDiscountCreateDTO dto) {
        return OrderDiscount.builder()
                .name(dto.getName())
                .percentage(dto.getPercentage())
                .amount(dto.getAmount())
                .orderLevel(Boolean.TRUE.equals(dto.getOrderLevel()))
                .build();
    }

    public OrderDiscountResponseDTO toOrderDiscountResponse(OrderDiscount entity) {
        Long lineItemId = entity.getLineItem() != null ? entity.getLineItem().getId() : null;
        return OrderDiscountResponseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .percentage(entity.getPercentage())
                .amount(entity.getAmount())
                .orderLevel(entity.isOrderLevel())
                .lineItemId(lineItemId)
                .build();
    }
}
