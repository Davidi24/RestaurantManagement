package pos.pos.Entity.Recipe;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "recipe", indexes = @Index(name="ix_recipe_name", columnList = "name", unique = true))
@SequenceGenerator(name="recipe_seq", sequenceName="recipe_seq", allocationSize=50)
public class Recipe {

  @Id
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="recipe_seq")
  private Long id;

  @Column(nullable=false, length=160)
  private String name;

  @Column(length=512)
  private String description;

  @Column(nullable=false, precision=19, scale=6)
  private BigDecimal portionYield;

  @Builder.Default
  @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<RecipeLine> lines = new ArrayList<>();

  @Version
  private long version;

  @CreationTimestamp @Column(nullable=false, updatable=false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp @Column(nullable=false)
  private OffsetDateTime updatedAt;

  public void setLines(List<RecipeLine> ls) {
    if (this.lines == null) this.lines = new ArrayList<>();
    this.lines.clear();
    if (ls != null) {
      for (RecipeLine l : ls) addLine(l);
    }
  }

  public void addLine(RecipeLine l) {
    if (l == null) return;
    l.setRecipe(this);
    this.lines.add(l);
  }
}
