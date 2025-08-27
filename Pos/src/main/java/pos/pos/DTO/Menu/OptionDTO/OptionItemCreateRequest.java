package pos.pos.DTO.Menu.OptionDTO;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record OptionItemCreateRequest(
        @NotBlank String name,
        @PositiveOrZero BigDecimal priceDelta,
        @PositiveOrZero Integer sortOrder
) {}

