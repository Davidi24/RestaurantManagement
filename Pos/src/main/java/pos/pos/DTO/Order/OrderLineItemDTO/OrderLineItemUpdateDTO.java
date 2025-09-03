package pos.pos.DTO.Order.OrderLineItemDTO;

import lombok.*;
import jakarta.validation.constraints.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderLineItemUpdateDTO {
    @NotNull
    private Long id;

    @NotNull
    @Min(1)
    private Integer quantity;

    private String itemName;
}
