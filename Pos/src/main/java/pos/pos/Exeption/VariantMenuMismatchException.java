// 400 – variant does not belong to the given menu item
package pos.pos.Exeption;

public class VariantMenuMismatchException extends RuntimeException {
    public VariantMenuMismatchException() {
        super("Variant does not belong to the provided MenuItem");
    }
}
