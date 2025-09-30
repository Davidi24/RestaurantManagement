package pos.pos.Entity.Recipe;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import pos.pos.Entity.Inventory.InventoryItem;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "ingredient",
  uniqueConstraints = @UniqueConstraint(name = "uk_ingredient_name", columnNames = "name"),
  indexes = @Index(name = "ix_ingredient_name", columnList = "name"))
@SequenceGenerator(name="ingredient_seq", sequenceName="ingredient_seq", allocationSize=50)
public class Ingredient {
  public enum UnitOfMeasure { G, KG, ML, L, PCS }

  @Id @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ingredient_seq")
  private Long id;

  @Column(nullable=false, length=128) private String name;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=16) private UnitOfMeasure stockUnit;
  @Column(precision=19, scale=6) private BigDecimal costPerStockUnit;
  @Column(length=256) private String allergenInfo;

  @Version private long version;
  @CreationTimestamp @Column(nullable=false, updatable=false) private OffsetDateTime createdAt;
  @UpdateTimestamp @Column(nullable=false) private OffsetDateTime updatedAt;

  @OneToOne(mappedBy="ingredient", fetch=FetchType.LAZY)
  private InventoryItem inventoryItem; // optional link
}
