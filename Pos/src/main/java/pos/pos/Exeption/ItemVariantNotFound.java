package pos.pos.Exeption;

public class ItemVariantNotFound extends RuntimeException {
    public ItemVariantNotFound(Long menuId, Long sectionId, Long itemId, Long variantId) {
        super("Variant with id " + variantId + " not found with menu id " + menuId + " and section id " + sectionId + " and item id " + itemId);
    }
}
