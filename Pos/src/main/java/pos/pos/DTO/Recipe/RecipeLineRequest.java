package pos.pos.DTO.Recipe;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record RecipeLineRequest(
        @NotNull(message = "ingredientId is required")
        Long ingredientId,

        @NotNull(message = "quantityPerPortion is required")
        @Positive(message = "quantityPerPortion must be greater than zero")
        BigDecimal quantityPerPortion
) {}

