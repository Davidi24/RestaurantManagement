package pos.pos.Entity.Inventory;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pos.pos.Entity.Recipe.Ingredient;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "inventory_item",
        uniqueConstraints = @UniqueConstraint(name="uk_inventory_item_ingredient", columnNames = "ingredient_id"),
        indexes = @Index(name="ix_inventory_item_name", columnList = "name"))
@SequenceGenerator(name="inventory_item_seq", sequenceName="inventory_item_seq", allocationSize=50)
public class InventoryItem {

  @Id
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="inventory_item_seq")
  private Long id;

  @OneToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name="ingredient_id", nullable=false, unique = true)
  private Ingredient ingredient;

  @Column(nullable=false, length=128)
  private String name;

  @Column(precision=19, scale=6)
  private BigDecimal reorderLevel;

  @Column(precision=19, scale=6)
  private BigDecimal minLevel;

  @Column(precision=19, scale=6)
  private BigDecimal targetLevel;

  @Column(precision=19, scale=6)
  private BigDecimal safetyStock;

  @Version
  private long version;

  @CreationTimestamp @Column(nullable=false, updatable=false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp @Column(nullable=false)
  private OffsetDateTime updatedAt;
}