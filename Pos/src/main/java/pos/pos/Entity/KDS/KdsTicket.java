package pos.pos.Entity.KDS;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

import pos.pos.Entity.Order.OrderStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class KdsTicket {

    private Long orderId;

    private String orderNumber;

    private Long tableId;

    private String notes;

    private OrderStatus status;

    private LocalDateTime openedAt;

    private List<KdsTicketItem> items;
}
