package pos.pos.Entity.Notification;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(
        name = "notification_events",
        indexes = {
                @Index(name = "idx_notif_channel_id", columnList = "channel,id"),
                @Index(name = "idx_notif_created_at", columnList = "created_at")
        }
)
public class NotificationEvent {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String channel;

    @Column(nullable = false, length = 128)
    private String event;

    @Lob @Column(nullable = false)
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist void pre() { if (createdAt == null) createdAt = Instant.now(); }
}
