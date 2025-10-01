package pos.pos.Repository.Inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pos.pos.Entity.Inventory.InventoryItem;
import java.util.List;
import java.util.Optional;

public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    Optional<InventoryItem> findByIngredientId(Long ingredientId);

    @Query("select i from InventoryItem i where i.quantityOnHand < i.reorderLevel")
    List<InventoryItem> findBelowReorder();
}
