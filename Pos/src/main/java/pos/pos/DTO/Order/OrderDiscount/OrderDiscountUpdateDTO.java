package pos.pos.DTO.Order.OrderDiscount;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderDiscountUpdateDTO {
    private Long id;
    private String name;
    private Double percentage;
    private Double amount;
}
