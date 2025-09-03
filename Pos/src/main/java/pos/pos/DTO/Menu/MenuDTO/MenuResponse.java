package pos.pos.DTO.Menu.MenuDTO;

import java.util.UUID;

public record MenuResponse(
        Long id,
        String name,
        String description,
        UUID publicId
) {}
