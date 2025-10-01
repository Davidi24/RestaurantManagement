package pos.pos.Service.Interfecaes.Inventory;

import pos.pos.DTO.Inventory.InventoryItemCreateRequest;
import pos.pos.Entity.Inventory.InventoryItem;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface InventoryService {
    InventoryItem createForIngredient(Long ingredientId);
    InventoryItem createForIngredient(InventoryItemCreateRequest inventoryItemCreateRequest);
    Optional<InventoryItem> findById(Long id);
    Optional<InventoryItem> findByIngredientId(Long ingredientId);
    List<InventoryItem> listAll();
    InventoryItem adjustQuantity(Long inventoryItemId, BigDecimal delta);
    InventoryItem setQuantity(Long inventoryItemId, BigDecimal quantity);
    List<InventoryItem> findBelowReorder();
    void delete(Long id);
}
