package pos.pos.Entity.Menu;

import jakarta.persistence.*;
import lombok.*;
import pos.pos.Entity.Recipe.Recipe;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", foreignKey = @ForeignKey(name = "fk_itemvariant_recipe"))
    private Recipe recipe;

    @Builder.Default
    @Column(precision = 10, scale = 4)
    private BigDecimal recipePortionFactor = new BigDecimal("1.0000");


    @PrePersist
    private void ensurePublicId() {
        if (publicId == null) {
            publicId = UUID.randomUUID();
        }
    }
}
