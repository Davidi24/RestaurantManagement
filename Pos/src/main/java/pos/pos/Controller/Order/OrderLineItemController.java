package pos.pos.Controller.Order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pos.pos.Config.ApiPaths;
import pos.pos.Config.Security.AuthUtils;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemCreateDTO;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemResponseDTO;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemUpdateDTO;
import pos.pos.Entity.Order.FulfillmentStatus;
import pos.pos.Service.Interfecaes.OrderLineItemService;

import java.util.List;

@RestController
@RequestMapping(value = ApiPaths.Order.LINE_ITEMS, produces = "application/json")
@RequiredArgsConstructor
public class OrderLineItemController {

    private final OrderLineItemService orderLineItemService;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<OrderLineItemResponseDTO> createLineItem(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderLineItemCreateDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(orderLineItemService.addLineItem(orderId, dto, authUtils.getUserEmail(authentication)));
    }

    @PutMapping
    public ResponseEntity<OrderLineItemResponseDTO> updateLineItem(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderLineItemUpdateDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(orderLineItemService.updateLineItem(orderId, dto, authUtils.getUserEmail(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<OrderLineItemResponseDTO>> getLineItems(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderLineItemService.getLineItems(orderId));
    }

    @DeleteMapping("/{lineItemId}")
    public ResponseEntity<Void> deleteLineItem(
            @PathVariable Long orderId,
            @PathVariable Long lineItemId,
            Authentication authentication) {
        orderLineItemService.deleteLineItem(orderId, lineItemId, authUtils.getUserEmail(authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{lineItemId}")
    public ResponseEntity<OrderLineItemResponseDTO> getLineItemById(
            @PathVariable Long orderId,
            @PathVariable Long lineItemId) {
        return ResponseEntity.ok(orderLineItemService.getLineItemById(orderId, lineItemId));
    }

    @PutMapping("/{lineItemId}/fulfillment")
    public ResponseEntity<OrderLineItemResponseDTO> setFulfillment(
            @PathVariable Long orderId,
            @PathVariable Long lineItemId,
            @RequestParam FulfillmentStatus status,
            Authentication authentication) {
        return ResponseEntity.ok(orderLineItemService.updateFulfillmentStatus(
                orderId, lineItemId, status, authUtils.getUserEmail(authentication)));
    }
}
