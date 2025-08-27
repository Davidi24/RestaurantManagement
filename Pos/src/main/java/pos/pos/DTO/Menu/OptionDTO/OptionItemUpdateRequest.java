package pos.pos.DTO.Menu.OptionDTO;

import java.math.BigDecimal;

public record OptionItemUpdateRequest(
        String name,
        BigDecimal priceDelta,
        Integer sortOrder
) {}
