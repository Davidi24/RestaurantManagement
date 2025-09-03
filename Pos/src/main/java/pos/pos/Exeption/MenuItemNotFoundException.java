// 404 – specific resource by publicId not found
package pos.pos.Exeption;

import java.util.UUID;

public class MenuItemNotFoundException extends RuntimeException {
    public MenuItemNotFoundException(UUID publicId) {
        super("MenuItem not found for publicId=" + publicId);
    }
}
