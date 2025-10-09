package pos.pos.DTO.Mapper.Inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pos.pos.DTO.Inventory.InventoryItemRequest;
import pos.pos.DTO.Inventory.InventoryItemResponse;

import pos.pos.DTO.Inventory.MovementRequest;
import pos.pos.DTO.Inventory.MovementResponse;
import pos.pos.Entity.Inventory.InventoryItem;
import pos.pos.Entity.Inventory.StockMovement;
import pos.pos.Entity.Recipe.Ingredient;
import pos.pos.Exeption.General.NotFoundException;
import pos.pos.Repository.Recipe.IngredientRepository;

@Component
@RequiredArgsConstructor
public class InventoryMapper {

    private final IngredientRepository ingredientRepository;

    public InventoryItem toEntity(InventoryItemRequest req) {
        Ingredient ing = ingredientRepository.findById(req.ingredientId())
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));
        return InventoryItem.builder()
                .ingredient(ing)
                .name(req.name().trim())
                .reorderLevel(req.reorderLevel())
                .minLevel(req.minLevel())
                .targetLevel(req.targetLevel())
                .safetyStock(req.safetyStock())
                .build();
    }

    public void updateEntity(InventoryItem e, InventoryItemRequest req) {
        if (req.name() != null && !req.name().isBlank()) e.setName(req.name().trim());
        if (req.reorderLevel() != null) e.setReorderLevel(req.reorderLevel());
        if (req.minLevel() != null) e.setMinLevel(req.minLevel());
        if (req.targetLevel() != null) e.setTargetLevel(req.targetLevel());
        if (req.safetyStock() != null) e.setSafetyStock(req.safetyStock());
    }

    public InventoryItemResponse toResponse(InventoryItem e, java.math.BigDecimal currentQty) {
        return InventoryItemResponse.builder()
                .id(e.getId())
                .ingredientId(e.getIngredient().getId())
                .name(e.getName())
                .currentQuantity(currentQty)
                .reorderLevel(e.getReorderLevel())
                .minLevel(e.getMinLevel())
                .targetLevel(e.getTargetLevel())
                .safetyStock(e.getSafetyStock())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public StockMovement toMovement(InventoryItem item, MovementRequest req, String createdBy) {
        return StockMovement.builder()
                .inventoryItem(item)
                .type(req.type())
                .quantity(req.quantity())
                .reason(req.reason())
                .reference(req.reference())
                .createdBy(createdBy)
                .build();
    }

    public MovementResponse toResponse(StockMovement m) {
        return MovementResponse.builder()
                .id(m.getId())
                .inventoryItemId(m.getInventoryItem().getId())
                .type(m.getType())
                .quantity(m.getQuantity())
                .reason(m.getReason())
                .reference(m.getReference())
                .occurredAt(m.getOccurredAt())
                .createdBy(m.getCreatedBy())
                .build();
    }
}