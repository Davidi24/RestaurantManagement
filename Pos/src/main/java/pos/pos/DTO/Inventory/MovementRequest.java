package pos.pos.DTO.Inventory;

import jakarta.validation.constraints.*;
import lombok.Builder;
import pos.pos.Entity.Inventory.MovementType;

import java.math.BigDecimal;

@Builder
public record MovementRequest(
        @NotNull MovementType type,
        @NotNull @DecimalMin(value = "0.000001") BigDecimal quantity,
        String reason,
        String reference
) {}