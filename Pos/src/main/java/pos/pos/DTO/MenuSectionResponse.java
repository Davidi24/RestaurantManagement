package pos.pos.DTO;

import java.util.List;

public record MenuSectionResponse(
        Long id,
        String name,
        Integer sortOrder,
        List<MenuItemSummary> items // lightweight summary to avoid heavy fetch
) {
    public record MenuItemSummary(Long id, String name, Integer sortOrder) {}
}
