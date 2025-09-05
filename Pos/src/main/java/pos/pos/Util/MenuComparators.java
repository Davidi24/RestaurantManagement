package pos.pos.Util;

import pos.pos.Entity.Menu.*;
import java.math.BigDecimal;
import java.util.Comparator;

public final class MenuComparators {
    private MenuComparators() {}

    public static final Comparator<MenuItem> ITEM_ORDER =
            Comparator.comparing(MenuItem::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                      .thenComparing(MenuItem::getId);

    public static final Comparator<MenuSection> SECTION_ORDER =
            Comparator.comparing(MenuSection::getOrderKey, Comparator.nullsLast(BigDecimal::compareTo))
                      .thenComparing(MenuSection::getId);

    public static final Comparator<ItemVariant> VARIANT_ORDER =
            Comparator.comparing(ItemVariant::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                      .thenComparing(ItemVariant::getId);

    public static final Comparator<OptionGroup> OPTION_GROUP_ORDER =
            Comparator.comparing(OptionGroup::getSortOrder, Comparator.nullsLast(Integer::compareTo))
                      .thenComparing(OptionGroup::getId);
}
