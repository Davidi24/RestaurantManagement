package pos.pos.Repository.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import pos.pos.Entity.Order.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
