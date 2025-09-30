package pos.pos.Entity.Order;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(
    name = "order_ingredient_snapshot",
    indexes = {
        @Index(name = "ix_ing_snap_line_item", columnList = "line_item_id"),
        @Index(name = "ix_ing_snap_ingredient_id", columnList = "ingredientId")
    }
)
public class OrderIngredientSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_item_id", nullable = false,
        foreignKey = @ForeignKey(name = "fk_ing_snap_line_item"))
    private OrderLineItem lineItem;

    @Column(nullable = false)
    private Long ingredientId;

    @Column(nullable = false, length = 160)
    private String ingredientName;

    @Column(nullable = false, length = 16)
    private String unit;

    @Column(nullable = false)
    private Double quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    @Builder.Default
    private SourceType sourceType = SourceType.BASE_RECIPE;


    private Long sourceRefId;

    @Column(length = 256)
    private String notes;

    public enum SourceType {
        BASE_RECIPE,
        OPTION_DELTA
    }
}
