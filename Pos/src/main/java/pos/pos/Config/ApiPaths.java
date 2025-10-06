package pos.pos.Config;

public final class ApiPaths {
    private ApiPaths() {}

    public static final String V1 = "/api/v1";

    public static final class Menu {
        public static final String BASE = V1 + "/menus";
        public static final String SECTION = BASE + "/{menuId}/sections";
        public static final String ITEM = SECTION + "/{sectionId}/items";
        public static final String VARIANT = ITEM + "/{itemId}/variants";
        public static final String OPTION_GROUP = ITEM + "/{itemId}/option-groups";
        public static final String OPTION_ITEM = OPTION_GROUP + "/{groupId}/options";
    }

    public static final class Order {
        public static final String BASE = V1 + "/orders";
        public static final String LINE_ITEMS = BASE + "/{orderId}/line-items";
        public static final String DISCOUNTS = BASE + "/{orderId}/discounts";
        public static final String EVENTS = BASE + "/{orderId}/events";
        public static final String TOTALS = BASE + "/{orderId}/totals";
    }

    public static final class Notification {
        public static final String BASE = V1 + "/notifications";
    }

    public static final class Kds {
        public static final String BASE = V1 + "/kds";
    }

    public static final class Inventory {
        public static final String BASE = V1 + "/inventory";
    }

    public static final class Ingredient {
        public static final String BASE = V1 + "/ingredient";
    }
}
