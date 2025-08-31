package pos.pos.Controller.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemCreateDTO;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemResponseDTO;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemUpdateDTO;
import pos.pos.Service.Interfecaes.OrderLineItemService;

import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/line-items")
@RequiredArgsConstructor
public class OrderLineItemController {

    private final OrderLineItemService orderLineItemService;

    @PostMapping
    public ResponseEntity<OrderLineItemResponseDTO> addLineItem(
            @PathVariable Long orderId,
            @RequestBody OrderLineItemCreateDTO dto) {
        return ResponseEntity.ok(orderLineItemService.addLineItem(orderId, dto));
    }

    @PutMapping("/{lineItemId}")
    public ResponseEntity<OrderLineItemResponseDTO> updateLineItem(
            @PathVariable Long orderId,
            @PathVariable Long lineItemId,
            @RequestBody OrderLineItemUpdateDTO dto) {
        dto.setId(lineItemId);
        return ResponseEntity.ok(orderLineItemService.updateLineItem(orderId, dto));
    }

    @GetMapping
    public ResponseEntity<List<OrderLineItemResponseDTO>> getLineItems(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderLineItemService.getLineItems(orderId));
    }

    @DeleteMapping("/{lineItemId}")
    public ResponseEntity<Void> deleteLineItem(@PathVariable Long orderId, @PathVariable Long lineItemId) {
        orderLineItemService.deleteLineItem(orderId, lineItemId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{lineItemId}")
    public ResponseEntity<OrderLineItemResponseDTO> getLineItemById(
            @PathVariable Long orderId,
            @PathVariable Long lineItemId) {
        return ResponseEntity.ok(orderLineItemService.getLineItemById(orderId, lineItemId));
    }
}
