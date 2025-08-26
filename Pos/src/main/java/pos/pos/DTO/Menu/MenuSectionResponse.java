package pos.pos.DTO.Menu;

import java.math.BigDecimal;
import java.util.List;

public record MenuSectionResponse(
        Long id,
        String name,
        Integer position,
        BigDecimal orderKey,
        List<MenuItemSummary> items
) {
    public record MenuItemSummary(Long id, String name, Integer sortOrder) {}
}
