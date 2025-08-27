package pos.pos.DTO.Menu.MenuSectionDTO;

import pos.pos.DTO.Menu.MenuItemDTO.MenuItemResponse;

import java.math.BigDecimal;
import java.util.List;

public record MenuSectionResponse(
        Long id,
        String name,
        Integer position,
        BigDecimal orderKey,
        List<MenuItemResponse> items
) {
}
