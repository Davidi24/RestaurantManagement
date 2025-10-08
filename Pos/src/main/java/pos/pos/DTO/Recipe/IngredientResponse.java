package pos.pos.DTO.Recipe;

import lombok.Builder;
import pos.pos.Entity.Recipe.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record IngredientResponse(
        Long id,
        String name,
        UnitOfMeasure stockUnit,
        BigDecimal costPerStockUnit,
        String allergenInfo,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
