package pos.pos.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pos.pos.Entity.Order.OrderDiscount;

import java.util.List;

public interface OrderDiscountRepository extends JpaRepository<OrderDiscount, Long> {
    List<OrderDiscount> findByOrder_Id(Long orderId);
}
