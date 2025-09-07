package pos.pos.Exeption;

public class OpenOrderExistsException extends RuntimeException {
    public OpenOrderExistsException(Long tableId, Long orderId, String orderNumber) {
        super("Table " + tableId + " already has an OPEN order (id=" + orderId + ", number=" + orderNumber + ").");
    }
}
