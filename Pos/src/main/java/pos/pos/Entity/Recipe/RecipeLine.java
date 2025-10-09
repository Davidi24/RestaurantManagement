package pos.pos.Entity.Recipe;

import jakarta.persistence.*;
import lombok.*;
import pos.pos.Entity.Recipe.Ingredient;

import java.math.BigDecimal;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "recipe_line")
@SequenceGenerator(name="recipe_line_seq", sequenceName="recipe_line_seq", allocationSize=50)
public class RecipeLine {

  @Id
  @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="recipe_line_seq")
  private Long id;

  @ManyToOne(optional=false, fetch=FetchType.LAZY)
  @JoinColumn(name="recipe_id", nullable=false)
  private Recipe recipe;

  @ManyToOne(optional=false, fetch=FetchType.LAZY)
  @JoinColumn(name="ingredient_id", nullable=false)
  private Ingredient ingredient;

  @Column(nullable=false, precision=19, scale=6)
  private BigDecimal quantityPerPortion;
}
