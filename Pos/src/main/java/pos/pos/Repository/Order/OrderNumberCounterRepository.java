package pos.pos.Repository.Order;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pos.pos.Entity.Order.OrderNumberCounter;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface OrderNumberCounterRepository extends JpaRepository<OrderNumberCounter, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from OrderNumberCounter c where c.date = :date and c.tableId = :tableId")
    Optional<OrderNumberCounter> findForUpdate(@Param("date") LocalDate date, @Param("tableId") Long tableId);
}
