package pos.pos.DTO.Order.OrderLineItemDTO;

import lombok.*;
import jakarta.validation.constraints.*;
import java.util.List;
import java.util.UUID;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderLineItemCreateDTO {
    @NotNull
    private UUID menuItemPublicId;

    @NotNull
    @Min(1)
    @Max(500)
    private Integer quantity;

    private UUID variantPublicId;
    private List<UUID> optionPublicIds;
}
