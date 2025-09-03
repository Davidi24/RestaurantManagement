package pos.pos.DTO.Menu.OptionDTO;

import java.math.BigDecimal;
import java.util.UUID;

public record OptionItemResponse(
        Long id,
        String name,
        BigDecimal priceDelta,
        Integer sortOrder,
        Long groupId,
        UUID publicId
) {}
