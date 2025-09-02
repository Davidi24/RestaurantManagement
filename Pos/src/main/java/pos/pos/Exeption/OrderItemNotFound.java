package pos.pos.Exeption;

public class OrderItemNotFound extends RuntimeException {

    public OrderItemNotFound(Long orderId, Long orderItemId) {
        super("Order item with ID " + orderItemId + " not found in order " + orderId);
    }
}
