package pos.pos.Repository.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import pos.pos.Entity.Order.Order;
import pos.pos.Entity.Order.OrderStatus;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findFirstByTableIdAndStatus(Long tableId, OrderStatus status);
}
