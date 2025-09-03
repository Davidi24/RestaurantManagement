package pos.pos.Entity.Menu;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class ItemVariant {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    private String name;
    private BigDecimal priceOverride;
    @Builder.Default
    private boolean isDefault = false;
    @Builder.Default
    private Integer sortOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    private MenuItem item;

    @PrePersist
    private void ensurePublicId() {
        if (publicId == null) {
            publicId = UUID.randomUUID();
        }
    }
}
