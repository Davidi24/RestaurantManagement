package pos.pos.DTO.Inventory;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record InventoryItemResponse(
        Long id,
        Long ingredientId,
        String name,
        BigDecimal currentQuantity,
        BigDecimal reorderLevel,
        BigDecimal minLevel,
        BigDecimal targetLevel,
        BigDecimal safetyStock,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}