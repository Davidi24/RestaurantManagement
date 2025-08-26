package pos.pos.DTO.Menu;

import jakarta.validation.constraints.*;

public record MenuSectionCreateRequest(
        @NotBlank String name,
        @PositiveOrZero Integer sortOrder
) {}
