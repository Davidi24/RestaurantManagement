package pos.pos.DTO.Menu.MenuDTO;

import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionResponse;

import java.util.List;

public record MenuTreeResponse(
        Long id,
        String name,
        String description,
        List<MenuSectionResponse> sections
) {

}
