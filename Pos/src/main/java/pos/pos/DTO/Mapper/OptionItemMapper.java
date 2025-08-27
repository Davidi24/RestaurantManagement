package pos.pos.DTO.Mapper;

import org.springframework.stereotype.Component;
import pos.pos.DTO.Menu.OptionDTO.OptionItemCreateRequest;
import pos.pos.DTO.Menu.OptionDTO.OptionItemResponse;
import pos.pos.DTO.Menu.OptionDTO.OptionItemUpdateRequest;
import pos.pos.Entity.Menu.OptionGroup;
import pos.pos.Entity.Menu.OptionItem;

@Component
public class OptionItemMapper {

    public OptionItem toEntity(OptionItemCreateRequest req, OptionGroup parentGroup) {
        OptionItem item = new OptionItem();
        item.setName(req.name());
        item.setPriceDelta(req.priceDelta() != null ? req.priceDelta() : item.getPriceDelta());
        item.setSortOrder(req.sortOrder() != null ? req.sortOrder() : 0);
        item.setGroup(parentGroup);
        return item;
    }

    public void apply(OptionItemUpdateRequest req, OptionItem entity) {
        if (req.name() != null) entity.setName(req.name());
        if (req.priceDelta() != null) entity.setPriceDelta(req.priceDelta());
        if (req.sortOrder() != null) entity.setSortOrder(req.sortOrder());
    }

    public OptionItemResponse toResponse(OptionItem entity) {
        return new OptionItemResponse(
                entity.getId(),
                entity.getName(),
                entity.getPriceDelta(),
                entity.getSortOrder(),
                entity.getGroup() != null ? entity.getGroup().getId() : null
        );
    }
}
