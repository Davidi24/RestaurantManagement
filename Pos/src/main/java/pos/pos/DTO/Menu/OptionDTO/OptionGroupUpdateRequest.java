package pos.pos.DTO.Menu.OptionDTO;

import pos.pos.Entity.Menu.OptionGroupType;

public record OptionGroupUpdateRequest(
    String name,
    OptionGroupType type,
    Boolean required,
    Integer minSelections,
    Integer maxSelections,
    Integer sortOrder
) {}
