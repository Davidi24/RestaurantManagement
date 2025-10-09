package pos.pos.DTO.Recipe.Ingredient;

import lombok.Builder;
import pos.pos.Entity.Recipe.UnitOfMeasure;
import java.math.BigDecimal;

@Builder
public record IngredientUpdateRequest(
        String name,
        UnitOfMeasure stockUnit,
        BigDecimal costPerStockUnit,
        String allergenInfo
) {}