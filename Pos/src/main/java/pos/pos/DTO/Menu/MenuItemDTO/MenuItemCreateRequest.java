package pos.pos.DTO.Menu.MenuItemDTO;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record MenuItemCreateRequest(
        @NotBlank String name,
        @PositiveOrZero BigDecimal basePrice,
        boolean available,
        @PositiveOrZero Integer sortOrder
        // TODO: later add variant & option group create requests if needed
) {}
