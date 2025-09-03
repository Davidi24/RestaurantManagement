package pos.pos.DTO.Menu.OptionDTO;

import pos.pos.Entity.Menu.OptionGroupType;

import java.util.List;
import java.util.UUID;


public record OptionGroupResponse(
        Long id,
        String name,
        OptionGroupType type,
        boolean required,
        Integer minSelections,
        Integer maxSelections,
        Integer sortOrder,
        Long itemId,
        UUID publicId,
    List<OptionItemResponse> options
) {}
