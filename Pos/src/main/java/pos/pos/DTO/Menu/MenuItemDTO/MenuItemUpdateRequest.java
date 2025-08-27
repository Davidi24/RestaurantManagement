// UPDATE (PATCH) — all fields optional (nullable) so you can partial-update
package pos.pos.DTO.Menu.MenuItemDTO;

import java.math.BigDecimal;

public record MenuItemUpdateRequest(
        String name,
        BigDecimal basePrice,
        Boolean available,
        Integer sortOrder
        // later: variants & option groups updates
) {}
