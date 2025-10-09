package pos.pos.Service.Interfecaes.Inventory;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pos.pos.DTO.Inventory.InventoryItemRequest;
import pos.pos.DTO.Inventory.InventoryItemResponse;
import pos.pos.DTO.Inventory.MovementRequest;
import pos.pos.DTO.Inventory.MovementResponse;

import java.time.OffsetDateTime;

public interface InventoryService {
    InventoryItemResponse createItem(InventoryItemRequest request);
    InventoryItemResponse updateItem(Long id, InventoryItemRequest request);
    void deleteItem(Long id);
    InventoryItemResponse getItem(Long id);
    Page<InventoryItemResponse> listItems(String q, Pageable pageable);

    MovementResponse move(Long itemId, MovementRequest request, String createdBy) throws BadRequestException;
    Page<MovementResponse> listMovements(Long itemId, OffsetDateTime from, OffsetDateTime to, Pageable pageable);
}