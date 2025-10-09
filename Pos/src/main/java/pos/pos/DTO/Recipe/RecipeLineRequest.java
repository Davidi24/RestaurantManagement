package pos.pos.DTO.Recipe;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record RecipeLineRequest(
        Long ingredientId,
        BigDecimal quantityPerPortion
) {}

