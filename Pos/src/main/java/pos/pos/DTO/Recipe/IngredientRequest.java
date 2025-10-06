package pos.pos.DTO.Recipe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import pos.pos.Entity.Recipe.UnitOfMeasure;

import java.math.BigDecimal;

public record IngredientRequest(
        @NotBlank
        String name,

        @NotNull
        UnitOfMeasure stockUnit,

        @NotNull
        BigDecimal costPerStockUnit,

        String allergenInfo
) {
}
