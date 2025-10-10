package pos.pos.Controller.Inventory;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pos.pos.Config.ApiPaths;
import pos.pos.DTO.Inventory.InventoryItemRequest;
import pos.pos.DTO.Inventory.InventoryItemResponse;
import pos.pos.DTO.Inventory.MovementRequest;
import pos.pos.DTO.Inventory.MovementResponse;
import pos.pos.Service.Interfecaes.Inventory.InventoryService;

import java.time.OffsetDateTime;

@RestController
@RequestMapping(ApiPaths.Inventory.BASE)
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    @PostMapping("/items")
    public ResponseEntity<InventoryItemResponse> createItem(@Valid @RequestBody InventoryItemRequest req) {
        return ResponseEntity.ok(service.createItem(req));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<InventoryItemResponse> updateItem(@PathVariable Long id, @RequestBody InventoryItemRequest req) {
        return ResponseEntity.ok(service.updateItem(id, req));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        service.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<InventoryItemResponse> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(service.getItem(id));
    }

    @GetMapping("/items")
    public ResponseEntity<Page<InventoryItemResponse>> listItems(
            @RequestParam(value="q", required=false) String q,
            @RequestParam(value="page", defaultValue="0") int page,
            @RequestParam(value="size", defaultValue="20") int size) {
        return ResponseEntity.ok(service.listItems(q, PageRequest.of(page, size)));
    }

    @PostMapping("/items/{id}/move")
    public ResponseEntity<MovementResponse> move(@PathVariable Long id,
                                                 @Valid @RequestBody MovementRequest req,
                                                 @RequestHeader(value="X-User", required=false) String user) throws BadRequestException {
        return ResponseEntity.ok(service.move(id, req, user));
    }

    @GetMapping("/items/{id}/movements")
    public ResponseEntity<Page<MovementResponse>> movements(@PathVariable Long id,
                                                            @RequestParam(value="from", required=false) OffsetDateTime from,
                                                            @RequestParam(value="to", required=false) OffsetDateTime to,
                                                            @RequestParam(value="page", defaultValue="0") int page,
                                                            @RequestParam(value="size", defaultValue="20") int size) {
        return ResponseEntity.ok(service.listMovements(id, from, to, PageRequest.of(page, size)));
    }
}