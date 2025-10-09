package pos.pos.DTO.Recipe;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record RecipeResponse(
        Long id,
        String name,
        String description,
        BigDecimal portionYield,
        BigDecimal totalCostPerPortion,
        List<RecipeLineResponse> lines,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}