// Entity
package pos.pos.Entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "password_reset_codes", indexes = {
        @Index(name = "ix_prc_user_id", columnList = "user_id"),
        @Index(name = "ix_prc_expires_at", columnList = "expiresAt")
})
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
@ToString
public class PasswordResetCode {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 64)
    private String codeHash;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;

    @Builder.Default
    @Column(nullable = false)
    private Instant createdAt = Instant.now();
}
