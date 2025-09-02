package pos.pos.Exeption;

public class OrderNotFound extends RuntimeException {
    public OrderNotFound(Long orderId) {
        super(
                "Order with id " + orderId + " is not found!"
        );
    }
}
