package pos.pos.DTO.Menu.OptionDTO;

import java.math.BigDecimal;

public record OptionItemResponse(
        Long id,
        String name,
        BigDecimal priceDelta,
        Integer sortOrder,
        Long groupId
) {}
