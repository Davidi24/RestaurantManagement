package pos.pos.DTO.Order.OrderLineItemDTO;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderLineItemCreateDTO {
    private Long menuItemId;
    private String itemName;
    private Double unitPrice;
    private Integer quantity;
}