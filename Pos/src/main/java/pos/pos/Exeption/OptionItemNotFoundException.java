package pos.pos.Exeption;

public class OptionItemNotFoundException extends RuntimeException {
    public OptionItemNotFoundException(Long menuId, Long sectionId, Long itemId, Long groupId, Long optionId) {
        super("OptionItem not found: menuId=" + menuId +
              ", sectionId=" + sectionId +
              ", itemId=" + itemId +
              ", groupId=" + groupId +
              ", optionId=" + optionId);
    }
}
