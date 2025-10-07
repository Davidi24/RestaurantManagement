package pos.pos.DTO.Mapper.Recipe;

import org.springframework.stereotype.Component;
import pos.pos.DTO.Recipe.IngredientRequest;
import pos.pos.Entity.Recipe.Ingredient;

@Component
public class IngredientMapper {

    public Ingredient toIngredient(IngredientRequest ingredientRequest) {
      return  Ingredient.builder()
              .name(ingredientRequest.name())
              .stockUnit(ingredientRequest.stockUnit())
              .costPerStockUnit(ingredientRequest.costPerStockUnit())
              .allergenInfo(ingredientRequest.allergenInfo())
                .build();
    }
}
