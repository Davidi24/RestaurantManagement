package pos.pos.DTO.Menu.OptionDTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import pos.pos.Entity.Menu.OptionGroupType;


public record OptionGroupCreateRequest(
    @NotBlank String name,
    @NotNull OptionGroupType type,
    boolean required,
    @Min(0) Integer minSelections,
    @Min(0) Integer maxSelections,
    @PositiveOrZero Integer sortOrder
) {}

