package pos.pos.DTO.Menu;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record MenuItemUpdateRequest(
        @NotBlank String name,
        @PositiveOrZero BigDecimal basePrice,
        boolean available,
        @PositiveOrZero Integer sortOrder
        // TODO: later add variant & option group update requests if needed
) {}
