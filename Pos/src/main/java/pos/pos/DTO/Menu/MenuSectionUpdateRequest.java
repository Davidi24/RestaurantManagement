package pos.pos.DTO.Menu;

import jakarta.validation.constraints.*;

public record MenuSectionUpdateRequest(
        @NotBlank String name,
        @PositiveOrZero Integer sortOrder
) {}
