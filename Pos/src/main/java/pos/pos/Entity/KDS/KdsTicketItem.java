package pos.pos.Entity.KDS;

import lombok.*;
import java.time.LocalDateTime;
import pos.pos.Entity.Order.FulfillmentStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class KdsTicketItem {

    private Long lineItemId;

    private String itemName;

    private Integer quantity;

    private FulfillmentStatus fulfillmentStatus;

    private LocalDateTime firedAt;

    private LocalDateTime readyAt;
}
