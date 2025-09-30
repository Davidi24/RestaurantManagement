// src/main/java/com/yourapp/domain/recipe/RecipeIngredient.java
package pos.pos.Entity.Recipe;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name="recipe_ingredient",
  uniqueConstraints=@UniqueConstraint(name="uk_recipe_ingredient", columnNames={"recipe_id","ingredient_id"}),
  indexes={
    @Index(name="ix_recipe_ingredient_recipe", columnList="recipe_id"),
    @Index(name="ix_recipe_ingredient_ingredient", columnList="ingredient_id")
  })
@SequenceGenerator(name="recipe_ingredient_seq", sequenceName="recipe_ingredient_seq", allocationSize=50)
public class RecipeIngredient {
  @Id @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="recipe_ingredient_seq")
  private Long id;

  @ManyToOne(optional=false, fetch=FetchType.LAZY)
  @JoinColumn(name="recipe_id", nullable=false, foreignKey=@ForeignKey(name="fk_recipeingredient_recipe"))
  private Recipe recipe;

  @ManyToOne(optional=false, fetch=FetchType.LAZY)
  @JoinColumn(name="ingredient_id", nullable=false, foreignKey=@ForeignKey(name="fk_recipeingredient_ingredient"))
  private Ingredient ingredient;

  @Column(nullable=false, precision=19, scale=6) private BigDecimal quantity;
  @Enumerated(EnumType.STRING) @Column(nullable=false, length=16) private Ingredient.UnitOfMeasure unit;
  @Column(length=256) private String notes;
}
