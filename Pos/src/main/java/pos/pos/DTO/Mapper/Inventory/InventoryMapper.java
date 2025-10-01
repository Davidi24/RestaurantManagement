package pos.pos.DTO.Mapper.Inventory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pos.pos.DTO.Inventory.InventoryItemCreateRequest;
import pos.pos.Entity.Inventory.InventoryItem;

@Component
@RequiredArgsConstructor
public class InventoryMapper {
    public InventoryItem toInventoryItem(InventoryItemCreateRequest inventoryItemCreateRequest) {
        return InventoryItem.builder()

                .build();
    }
}
