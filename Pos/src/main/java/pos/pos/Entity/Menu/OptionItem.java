package pos.pos.Entity.Menu;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;
import pos.pos.Entity.Recipe.Recipe;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OptionItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    private String name;

    @Builder.Default
    private BigDecimal priceDelta = BigDecimal.ZERO;

    @Builder.Default
    private Integer sortOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    private OptionGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_delta_id", foreignKey = @ForeignKey(name = "fk_optionitem_recipe_delta"))
    private Recipe recipeDelta;

    @Builder.Default
    @Column(precision = 10, scale = 4)
    private BigDecimal recipeDeltaFactor = new BigDecimal("1.0000");

    @PrePersist
    private void ensurePublicId() {
        if (publicId == null) {
            publicId = UUID.randomUUID();
        }
    }
}
