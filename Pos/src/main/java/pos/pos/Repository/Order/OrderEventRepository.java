package pos.pos.Repository.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import pos.pos.Entity.Order.OrderEvent;

import java.util.List;

public interface OrderEventRepository extends JpaRepository<OrderEvent, Long> {
    List<OrderEvent> findByOrder_Id(Long orderId);
}
