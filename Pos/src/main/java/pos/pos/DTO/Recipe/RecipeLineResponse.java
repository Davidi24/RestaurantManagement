package pos.pos.DTO.Recipe;

import lombok.Builder;
import pos.pos.Entity.Recipe.UnitOfMeasure;

import java.math.BigDecimal;

@Builder
public record RecipeLineResponse(
        Long id,
        Long ingredientId,
        String ingredientName,
        UnitOfMeasure unit,
        BigDecimal quantityPerPortion,
        BigDecimal costPerPortion
) {}
