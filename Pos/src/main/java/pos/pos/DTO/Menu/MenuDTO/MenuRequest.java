package pos.pos.DTO.Menu.MenuDTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MenuRequest(
        @NotBlank @Size(max = 100) String name,
        @Size(max = 255) String description
) {}
