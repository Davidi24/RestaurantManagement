package pos.pos.Service.Inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Inventory.InventoryItemCreateRequest;
import pos.pos.Entity.Inventory.InventoryItem;
import pos.pos.Entity.Recipe.Ingredient;
import pos.pos.Exeption.General.NotFoundException;
import pos.pos.Repository.Inventory.InventoryItemRepository;
import pos.pos.Repository.Recipe.IngredientRepository;
import pos.pos.Service.Interfecaes.Inventory.InventoryService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {
    private final InventoryItemRepository inventoryRepo;
    private final IngredientRepository ingredientRepo;

    @Override
    public InventoryItem createForIngredient(Long ingredientId) {
        Ingredient ingredient = ingredientRepo.findById(ingredientId).orElseThrow(
                () -> new NotFoundException("Ingredient", ingredientId)
        );

        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setIngredient(ingredient);
        inventoryItem.setQuantityOnHand(BigDecimal.ZERO);
        inventoryItem.setReorderLevel(BigDecimal.ZERO);

        return inventoryRepo.save(inventoryItem);
    }

    @Override
    public InventoryItem createForIngredient(InventoryItemCreateRequest inventoryItemCreateRequest) {
        Ingredient ingredient = ingredientRepo.findById(inventoryItemCreateRequest.ingredientId()).orElseThrow(
                () -> new NotFoundException("Ingredient", inventoryItemCreateRequest.ingredientId())
        );

        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setIngredient(ingredient);
        inventoryItem.setQuantityOnHand(inventoryItemCreateRequest.initialQty());
        inventoryItem.setReorderLevel(inventoryItemCreateRequest.reorderLevel());

        return inventoryRepo.save(inventoryItem);
    }


    @Override
    public Optional<InventoryItem> findById(Long id) {
        return inventoryRepo.findById(id);
    }

    @Override
    public Optional<InventoryItem> findByIngredientId(Long ingredientId) {
        return inventoryRepo.findByIngredientId(ingredientId);
    }

    @Override
    public List<InventoryItem> listAll() {
        return inventoryRepo.findAll();
    }

    @Override
    public InventoryItem adjustQuantity(Long inventoryItemId, BigDecimal delta) {
        // impl
        return null;
    }

    @Override
    public InventoryItem setQuantity(Long inventoryItemId, BigDecimal quantity) {
        // impl
        return null;
    }

    @Override
    public List<InventoryItem> findBelowReorder() {
        return inventoryRepo.findBelowReorder();
    }

    @Override
    public void delete(Long id) {
        inventoryRepo.deleteById(id);
    }
}
