// 404 – variant not found
package pos.pos.Exeption;

import java.util.UUID;

public class ItemVariantNotFoundException extends RuntimeException {
    public ItemVariantNotFoundException(UUID publicId) {
        super("Variant not found for publicId=" + publicId);
    }
}
