package pos.pos.DTO.Mapper;


import pos.pos.DTO.Menu.MenuResponse;
import pos.pos.DTO.Menu.MenuTreeResponse;
import pos.pos.Entity.Menu.Menu;
import java.util.Comparator;
import pos.pos.Entity.Menu.MenuSection;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.ItemVariant;
import pos.pos.Entity.Menu.OptionGroup;
import pos.pos.Entity.Menu.OptionItem;
public class MenuMapper {

    private static final Comparator<Integer> sortNullSafe = Comparator.nullsFirst(Integer::compareTo);

    // --- Flat response ---
    public static MenuResponse toResponse(Menu menu) {
        return new MenuResponse(menu.getId(), menu.getName(), menu.getDescription());
    }

    // --- Nested tree response ---
    public static MenuTreeResponse toTreeResponse(Menu menu) {
        var sections = menu.getSections().stream()
                .sorted(Comparator.comparing(MenuSection::getSortOrder, sortNullSafe).thenComparing(MenuSection::getId))
                .map(sec -> new MenuTreeResponse.Section(
                        sec.getId(),
                        sec.getName(),
                        sec.getSortOrder(),
                        sec.getItems().stream()
                                .sorted(Comparator.comparing(MenuItem::getSortOrder, sortNullSafe).thenComparing(MenuItem::getId))
                                .map(item -> new MenuTreeResponse.Item(
                                        item.getId(),
                                        item.getName(),
                                        item.getBasePrice(),
                                        item.isAvailable(),
                                        item.getSortOrder(),
                                        item.getVariants().stream()
                                                .sorted(Comparator.comparing(ItemVariant::getSortOrder, sortNullSafe).thenComparing(ItemVariant::getId))
                                                .map(v -> new MenuTreeResponse.Variant(
                                                        v.getId(), v.getName(), v.getPriceOverride(), v.isDefault(), v.getSortOrder()
                                                ))
                                                .toList(),
                                        item.getOptionGroups().stream()
                                                .sorted(Comparator.comparing(OptionGroup::getSortOrder, sortNullSafe).thenComparing(OptionGroup::getId))
                                                .map(g -> new MenuTreeResponse.Group(
                                                        g.getId(),
                                                        g.getName(),
                                                        g.getType() == null ? null : g.getType().name(),
                                                        g.isRequired(),
                                                        g.getMinSelections(),
                                                        g.getMaxSelections(),
                                                        g.getSortOrder(),
                                                        g.getOptions().stream()
                                                                .sorted(Comparator.comparing(OptionItem::getSortOrder, sortNullSafe).thenComparing(OptionItem::getId))
                                                                .map(o -> new MenuTreeResponse.Option(
                                                                        o.getId(), o.getName(), o.getPriceDelta(), o.getSortOrder()
                                                                ))
                                                                .toList()
                                                ))
                                                .toList()
                                ))
                                .toList()
                ))
                .toList();

        return new MenuTreeResponse(menu.getId(), menu.getName(), menu.getDescription(), sections);
    }
}
