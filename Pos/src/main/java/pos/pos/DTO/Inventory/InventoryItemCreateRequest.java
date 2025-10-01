package pos.pos.DTO.Inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record InventoryItemCreateRequest(
        @NotNull
        Long ingredientId,
        @DecimalMin(value = "0", inclusive = true) @Digits(integer = 13, fraction = 6)
        BigDecimal initialQty,
        @DecimalMin(value = "0", inclusive = true) @Digits(integer = 13, fraction = 6)
        BigDecimal reorderLevel
) {}
