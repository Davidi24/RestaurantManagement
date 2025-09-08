package pos.pos.DTO.Order.OrderCollectorDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderUpdateDTO {

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    private Long tableId;

    @Min(value = 0, message = "Number of guests cannot be negative")
    private Long numberOfGuests;
}
