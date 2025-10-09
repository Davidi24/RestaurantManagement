package pos.pos.Repository.Inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pos.pos.Entity.Inventory.InventoryItem;

import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    boolean existsByIngredient_Id(Long ingredientId);
    Optional<InventoryItem> findByIngredient_Id(Long ingredientId);
}