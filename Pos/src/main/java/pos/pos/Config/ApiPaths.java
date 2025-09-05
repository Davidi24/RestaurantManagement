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
}
