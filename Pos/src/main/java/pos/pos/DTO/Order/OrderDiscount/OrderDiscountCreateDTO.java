package pos.pos.DTO.Order.OrderDiscount;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderDiscountCreateDTO {
    private String name;
    private Double percentage;
    private Double amount;
    private Boolean orderLevel;
    private Long lineItemId;
}
