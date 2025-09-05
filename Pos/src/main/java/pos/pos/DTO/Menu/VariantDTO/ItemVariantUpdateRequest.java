package pos.pos.DTO.Menu.VariantDTO;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;


public record ItemVariantUpdateRequest(
        String name,

        @PositiveOrZero(message = "priceOverride must be >= 0")
        @Digits(integer = 10, fraction = 2, message = "priceOverride must have max 2 decimal places")
        BigDecimal priceOverride,

        Boolean isDefault
) {}
