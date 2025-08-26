package pos.pos.DTO.Menu;

import java.math.BigDecimal;

public record MenuItemResponse(
        Long id,
        String name,
        BigDecimal basePrice,
        boolean available,
        Integer sortOrder
        // TODO: later add variant & option group summaries
) {}
