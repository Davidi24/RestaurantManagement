package pos.pos.DTO.Menu.MenuDTO;

import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionResponse;

import java.util.List;
import java.util.UUID;

public record MenuTreeResponse(
        Long id,
        String name,
        String description,
        UUID publicId,
        List<MenuSectionResponse> sections
) {}
