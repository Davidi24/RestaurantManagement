package pos.pos.Repository.Order;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pos.pos.Entity.Order.Order;
import pos.pos.Entity.Order.OrderStatus;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findFirstByTableIdAndStatus(Long tableId, OrderStatus status);
    // In OrderRepository
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findForUpdate(@Param("id") Long id);

}
