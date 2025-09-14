package pos.pos.Controller.Order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pos.pos.Config.ApiPaths;
import pos.pos.Config.Security.AuthUtils;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderCreateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderResponseDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderStatusUpdateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderUpdateDTO;
import pos.pos.Service.Interfecaes.Order.OrderService;

import java.net.URI;
import java.util.List;

//TODO: filter Order like by order number, date ...

@RestController
@RequestMapping(value = ApiPaths.Order.BASE, produces = "application/json")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderCreateDTO dto,
                                                        Authentication authentication) {
        OrderResponseDTO created = orderService.createOrder(dto, authUtils.getUserEmail(authentication));
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrder(@PathVariable Long id,
                                                        @Valid @RequestBody OrderUpdateDTO dto) {
        return ResponseEntity.ok(orderService.updateOrder(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    //TODO: only kitchen can change a specific role or the other way around
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(@PathVariable Long id,
                                                         @Valid @RequestBody OrderStatusUpdateDTO dto,
                                                         Authentication authentication) {
        return ResponseEntity.ok(orderService.updateStatus(id, dto, authUtils.getUserEmail(authentication)));
    }

    @PutMapping("/{id}/serve-all")
    public ResponseEntity<OrderResponseDTO> serveAll(@PathVariable Long id, Authentication auth) {
        String email = authUtils.getUserEmail(auth);
        return ResponseEntity.ok(orderService.serveAllItems(id, email));
    }

    @PutMapping("/{orderId}/items/{lineItemId}/serve")
    public ResponseEntity<OrderResponseDTO> serveOneItem(@PathVariable Long orderId,
                                                         @PathVariable Long lineItemId,
                                                         Authentication auth) {
        String email = authUtils.getUserEmail(auth);
        return ResponseEntity.ok(orderService.serveOneItem(orderId, lineItemId, email));
    }
}
