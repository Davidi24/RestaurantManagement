package pos.pos.Entity.KDS;

import lombok.*;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class KdsEvent {

    public enum Type {
        TICKET_CREATED,
        ITEM_UPDATED,
        TICKET_READY,
        TICKET_BUMPED,
        TICKET_RECALLED,
        TICKET_VOIDED
    }

    private Type eventType;

    private Long ticketId;

    private Object payload;

    private Instant timestamp;
}
