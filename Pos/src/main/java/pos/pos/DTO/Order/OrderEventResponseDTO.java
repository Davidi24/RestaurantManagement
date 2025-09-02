package pos.pos.DTO.Order;

import lombok.*;
import pos.pos.Entity.Order.OrderEventType;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderEventResponseDTO {
    private Long id;
    private OrderEventType type;
    private String staffEmail;
    private LocalDateTime timestamp;
    private String metadata;
}
