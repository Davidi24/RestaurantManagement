package pos.pos.Service.Interfecaes.Recipe;

import pos.pos.Entity.Recipe.Recipe;
import pos.pos.Entity.Recipe.UnitOfMeasure;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RecipeService {
    Recipe create(String name);
    Optional<Recipe> findById(Long id);
    List<Recipe> listAll();
    Recipe rename(Long id, String newName);
    Recipe addIngredient(Long recipeId, Long ingredientId, BigDecimal quantity, UnitOfMeasure unit, String notes);
    Recipe updateIngredient(Long recipeIngredientId, BigDecimal quantity, UnitOfMeasure unit, String notes);
    void removeIngredient(Long recipeId, Long recipeIngredientId);
    void delete(Long id);
}
