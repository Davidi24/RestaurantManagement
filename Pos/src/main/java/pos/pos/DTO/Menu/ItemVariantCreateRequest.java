package pos.pos.DTO.Menu;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;


public record ItemVariantCreateRequest(
        @NotBlank(message = "Variant name is required.")
        String name,

        @PositiveOrZero(message = "priceOverride must be >= 0")
        @Digits(integer = 10, fraction = 2, message = "priceOverride must have max 2 decimal places")
        BigDecimal priceOverride,

        Boolean isDefault,

        @Min(value = 0, message = "sortOrder must be >= 0")
        Integer sortOrder
) {}
