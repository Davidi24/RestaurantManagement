package pos.pos.DTO.Order.OrderCollectorDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderCreateDTO {
   @NonNull @Min(1)
   private Long tableId;
}
