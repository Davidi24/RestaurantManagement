package pos.pos.Repository.Inventory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pos.pos.Entity.Inventory.StockMovement;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("""
           select coalesce(sum(
             case m.type
               when pos.pos.Entity.Inventory.MovementType.IN then m.quantity
               when pos.pos.Entity.Inventory.MovementType.OUT then -m.quantity
               when pos.pos.Entity.Inventory.MovementType.ADJUST then m.quantity
             end
           ), 0)
           from StockMovement m
           where m.inventoryItem.id = :inventoryItemId
           """)
    BigDecimal computeCurrentQuantity(Long inventoryItemId);

    Page<StockMovement> findByInventoryItem_Id(Long inventoryItemId, Pageable pageable);

    Page<StockMovement> findByInventoryItem_IdAndOccurredAtBetween(Long inventoryItemId, OffsetDateTime from, OffsetDateTime to, Pageable pageable);
}