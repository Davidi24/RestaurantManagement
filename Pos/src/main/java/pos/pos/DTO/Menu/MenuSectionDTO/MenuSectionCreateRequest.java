package pos.pos.DTO.Menu.MenuSectionDTO;

import jakarta.validation.constraints.*;

public record MenuSectionCreateRequest(
        @NotBlank String name,
        @PositiveOrZero Integer sortOrder
) {}
