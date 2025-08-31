package pos.pos.DTO.Order.OrderCollectorDTO;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderCreateDTO {
    private Long staffId;
    private Long tableId;
    private Long customerId;
    private String notes;
}
