package pos.pos.DTO.Menu.MenuSectionDTO;

import pos.pos.DTO.Menu.MenuItemDTO.MenuItemResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MenuSectionResponse(
        Long id,
        String name,
        Integer position,
        BigDecimal orderKey,
        UUID publicId,
        List<MenuItemResponse> items
) {
}
