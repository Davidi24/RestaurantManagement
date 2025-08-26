package pos.pos.DTO;

import jakarta.validation.constraints.*;

public record MenuSectionCreateRequest(
        @NotBlank String name,
        @PositiveOrZero Integer sortOrder
) {}
