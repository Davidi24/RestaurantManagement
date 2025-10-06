package pos.pos.Controller.Inventory;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pos.pos.Config.ApiPaths;
import pos.pos.DTO.Inventory.InventoryItemCreateRequest;
import pos.pos.Entity.Inventory.InventoryItem;
import pos.pos.Service.Inventory.InventoryServiceImpl;

@RestController
@RequestMapping(ApiPaths.Inventory.BASE)
@RequiredArgsConstructor
public class InventoryItemController {

    private final InventoryServiceImpl inventoryService;

    @PostMapping
    public ResponseEntity<InventoryItem> createInventory(@Valid InventoryItemCreateRequest inventoryItemCreateRequest){
        return ResponseEntity.ok(inventoryService.createForIngredient(inventoryItemCreateRequest));
    }
}
