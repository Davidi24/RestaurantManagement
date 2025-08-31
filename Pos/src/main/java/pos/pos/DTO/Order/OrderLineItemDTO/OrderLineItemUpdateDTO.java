package pos.pos.DTO.Order.OrderLineItemDTO;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderLineItemUpdateDTO {
    private Long id;
    private Integer quantity;
    private String itemName;
}