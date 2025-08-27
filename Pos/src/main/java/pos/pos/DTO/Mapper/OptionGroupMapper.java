package pos.pos.DTO.Mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupCreateRequest;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupResponse;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupUpdateRequest;
import pos.pos.DTO.Menu.OptionDTO.OptionItemResponse;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.OptionGroup;
import pos.pos.Entity.Menu.OptionItem;

import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OptionGroupMapper {

    private final OptionItemMapper optionItemMapper;

    public OptionGroup toEntity(OptionGroupCreateRequest req, MenuItem parentItem) {
        OptionGroup group = new OptionGroup();
        group.setName(req.name());
        group.setType(req.type());
        group.setRequired(req.required());
        group.setMinSelections(req.minSelections());
        group.setMaxSelections(req.maxSelections());
        group.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        group.setItem(parentItem);
        return group;
    }

    public void apply(OptionGroupUpdateRequest req, OptionGroup entity) {
        if (req.name() != null) entity.setName(req.name());
        if (req.type() != null) entity.setType(req.type());
        if (req.required() != null) entity.setRequired(req.required());
        if (req.minSelections() != null) entity.setMinSelections(req.minSelections());
        if (req.maxSelections() != null) entity.setMaxSelections(req.maxSelections());
        if (req.sortOrder() != null) entity.setSortOrder(req.sortOrder());
    }

    public OptionGroupResponse toResponse(OptionGroup entity) {
        List<OptionItemResponse> options = entity.getOptions().stream()
                .sorted(Comparator
                        .comparing(OptionItem::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(OptionItem::getId))
                .map(optionItemMapper::toResponse)
                .toList();

        return new OptionGroupResponse(
                entity.getId(),
                entity.getName(),
                entity.getType(),
                entity.isRequired(),
                entity.getMinSelections(),
                entity.getMaxSelections(),
                entity.getSortOrder(),
                entity.getItem() != null ? entity.getItem().getId() : null,
                options
        );
    }
}
