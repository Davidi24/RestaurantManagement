package pos.pos.DTO.Order.OrderDiscount;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderDiscountResponseDTO {
    private Long id;
    private String name;
    private Double percentage;
    private Double amount;
    private boolean orderLevel;
    private Long lineItemId;
}
