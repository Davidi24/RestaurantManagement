package pos.pos.DTO.Order.OrderLineItemDTO;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderLineItemResponseDTO {
    private Long id;
    private Long menuItemId;
    private String itemName;
    private Double unitPrice;
    private Integer quantity;
    private Double lineSubtotal;
    private Double lineDiscount;
    private Double lineGrandTotal;
    private String notes;
}