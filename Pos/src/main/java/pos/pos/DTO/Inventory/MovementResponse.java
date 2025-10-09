package pos.pos.DTO.Inventory;

import lombok.Builder;
import pos.pos.Entity.Inventory.MovementType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record MovementResponse(
        Long id,
        Long inventoryItemId,
        MovementType type,
        BigDecimal quantity,
        String reason,
        String reference,
        OffsetDateTime occurredAt,
        String createdBy
) {}