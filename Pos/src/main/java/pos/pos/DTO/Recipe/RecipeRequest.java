package pos.pos.DTO.Recipe;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record RecipeRequest(
        @NotBlank @Size(max=160) String name,
        @Size(max=512) String description,
        @NotNull @DecimalMin(value="0.000001") BigDecimal portionYield,
        @NotNull @Size(min=1) List<@Valid @NotNull RecipeLineRequest> lines
) {}
