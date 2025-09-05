package pos.pos.DTO.Mapper.MenuMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pos.pos.DTO.Menu.VariantDTO.ItemVariantResponse;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemCreateRequest;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemResponse;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemUpdateRequest;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupResponse;
import pos.pos.Entity.Menu.ItemVariant;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.OptionGroup;

import java.util.List;

import static pos.pos.Util.MenuComparators.OPTION_GROUP_ORDER;
import static pos.pos.Util.MenuComparators.VARIANT_ORDER;

@Component
@RequiredArgsConstructor
public class MenuItemMapper {

    private final ItemVariantMapper itemVariantMapper;
    private final OptionGroupMapper optionGroupMapper;

    public MenuItem toMenuItem(MenuItemCreateRequest req) {
        MenuItem item = new MenuItem();
        item.setName(req.name());
        item.setBasePrice(req.basePrice());
        item.setAvailable(Boolean.TRUE.equals(req.available()));
        item.setSortOrder(req.sortOrder());
        return item;
    }

    public void apply(MenuItemUpdateRequest req, MenuItem item) {
        if (req.name() != null)       item.setName(req.name());
        if (req.basePrice() != null)  item.setBasePrice(req.basePrice());
        if (req.available() != null)  item.setAvailable(req.available());
        if (req.sortOrder() != null)  item.setSortOrder(req.sortOrder());
    }

    public MenuItemResponse toMenuItemResponse(MenuItem item) {
        List<ItemVariantResponse> variants =
                (item.getVariants() == null ? List.<ItemVariant>of() : item.getVariants())
                        .stream()
                        .sorted(VARIANT_ORDER)
                        .map(itemVariantMapper::toResponse)
                        .toList();

        List<OptionGroupResponse> optionGroups =
                (item.getOptionGroups() == null ? List.<OptionGroup>of() : item.getOptionGroups())
                        .stream()
                        .sorted(OPTION_GROUP_ORDER)
                        .map(optionGroupMapper::toResponse)
                        .toList();

        return new MenuItemResponse(
                item.getId(),
                item.getName(),
                item.getBasePrice(),
                item.isAvailable(),
                item.getSortOrder(),
                item.getPublicId(),
                variants,
                optionGroups
        );
    }
}
