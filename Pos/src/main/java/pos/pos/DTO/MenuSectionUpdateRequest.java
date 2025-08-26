package pos.pos.DTO;

import jakarta.validation.constraints.*;

public record MenuSectionUpdateRequest(
        @NotBlank String name,
        @PositiveOrZero Integer sortOrder
) {}
