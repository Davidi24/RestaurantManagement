package pos.pos.DTO.Menu.VariantDTO;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemVariantResponse(
        Long id,
        String name,
        BigDecimal priceOverride,
        boolean isDefault,
        Integer sortOrder,
        Long itemId,
        UUID publicId
) {}
