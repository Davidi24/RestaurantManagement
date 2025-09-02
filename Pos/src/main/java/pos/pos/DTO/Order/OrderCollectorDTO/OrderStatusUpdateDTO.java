package pos.pos.DTO.Order.OrderCollectorDTO;

import lombok.*;
import pos.pos.Entity.Order.OrderStatus;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderStatusUpdateDTO {
    private OrderStatus status;
}
