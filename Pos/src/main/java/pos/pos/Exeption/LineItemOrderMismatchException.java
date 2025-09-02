package pos.pos.Exeption;

public class LineItemOrderMismatchException extends RuntimeException {

    public LineItemOrderMismatchException(Long lineItemId, Long orderId) {
        super("LineItem " + lineItemId + " does not belong to order " + orderId);
    }
}
