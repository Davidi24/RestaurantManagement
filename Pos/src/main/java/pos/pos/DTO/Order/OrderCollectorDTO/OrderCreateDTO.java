package pos.pos.DTO.Order.OrderCollectorDTO;

import jakarta.validation.constraints.Min;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderCreateDTO {
   @NonNull @Min(1)
   private Long tableId;

   @NonNull @Min(1)
   private Long numberOfGuests;
}
