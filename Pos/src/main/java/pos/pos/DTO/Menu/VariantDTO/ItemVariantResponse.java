package pos.pos.DTO.Menu.VariantDTO;

import java.math.BigDecimal;

public record ItemVariantResponse(
        Long id,
        String name,
        BigDecimal priceOverride,
        boolean isDefault,
        Integer sortOrder,
        Long itemId
) {}
