package pos.pos.Exeption;

public class OptionGroupNotFoundException extends RuntimeException {
    public OptionGroupNotFoundException(Long menuId, Long sectionId, Long itemId, Long optionGroupId) {
        super("OptionGroup not found: menuId=" + menuId +
                ", sectionId=" + sectionId +
                ", itemId=" + itemId +
                ", optionGroupId=" + optionGroupId);
    }
}
