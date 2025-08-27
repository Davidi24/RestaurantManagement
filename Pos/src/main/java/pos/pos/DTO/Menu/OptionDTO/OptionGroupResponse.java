package pos.pos.DTO.Menu.OptionDTO;

import pos.pos.Entity.Menu.OptionGroupType;

import java.util.List;


public record OptionGroupResponse(
    Long id,
    String name,
    OptionGroupType type,
    boolean required,
    Integer minSelections,
    Integer maxSelections,
    Integer sortOrder,
    Long itemId,
    List<OptionItemResponse> options
) {}
