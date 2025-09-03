// 400 – option does not belong to the given menu item
package pos.pos.Exeption;

public class OptionMenuMismatchException extends RuntimeException {
    public OptionMenuMismatchException() {
        super("OptionItem does not belong to the provided MenuItem");
    }
}
