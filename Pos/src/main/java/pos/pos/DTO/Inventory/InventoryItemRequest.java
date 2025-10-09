package pos.pos.DTO.Inventory;

import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record InventoryItemRequest(
        @NotNull Long ingredientId,
        @NotBlank String name,
        BigDecimal reorderLevel,
        BigDecimal minLevel,
        BigDecimal targetLevel,
        BigDecimal safetyStock
) {}