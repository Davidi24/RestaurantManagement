
package pos.pos.Entity.Recipe;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.OffsetDateTime;
import java.util.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "recipe",
  uniqueConstraints = @UniqueConstraint(name="uk_recipe_name", columnNames="name"),
  indexes = @Index(name="ix_recipe_name", columnList="name"))
@SequenceGenerator(name="recipe_seq", sequenceName="recipe_seq", allocationSize=50)
public class Recipe {

  @Id @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="recipe_seq")
  private Long id;

  @Column(nullable=false, length=160)
  private String name;

  @Column(length=1000)
  private String instructions;

  @Version private long version;
  @CreationTimestamp @Column(nullable=false, updatable=false)
  private OffsetDateTime createdAt;

  @UpdateTimestamp @Column(nullable=false)
  private OffsetDateTime updatedAt;

  @OneToMany(mappedBy="recipe", cascade=CascadeType.ALL, orphanRemoval=true)
  @Builder.Default
  private List<RecipeIngredient> ingredients = new ArrayList<>();

  public void addIngredient(RecipeIngredient ri){ ri.setRecipe(this); ingredients.add(ri); }
  public void removeIngredient(RecipeIngredient ri){ ingredients.remove(ri); ri.setRecipe(null); }
}
