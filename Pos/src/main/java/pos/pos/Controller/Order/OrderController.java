package pos.pos.Controller.Order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pos.pos.Config.Security.AuthUtils;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderCreateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderResponseDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderStatusUpdateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderUpdateDTO;
import pos.pos.Service.Interfecaes.OrderService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final AuthUtils authUtils;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderCreateDTO dto, Authentication authentication) {
        String email = authUtils.getUserEmail(authentication);
        return ResponseEntity.ok(orderService.createOrder(dto, email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderUpdateDTO dto) {
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
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponseDTO> updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusUpdateDTO dto) {
        return ResponseEntity.ok(orderService.updateStatus(id, dto));
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<OrderResponseDTO> close(@PathVariable Long id, Authentication auth) {
        String email = authUtils.getUserEmail(auth);
        return ResponseEntity.ok(orderService.closeOrder(id, email));
    }

    @PostMapping("/{id}/void")
    public ResponseEntity<OrderResponseDTO> voidOrder(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body, Authentication auth) {
        String email = authUtils.getUserEmail(auth);
        String reason = body != null ? body.getOrDefault("reason", "Void") : "Void";
        return ResponseEntity.ok(orderService.voidOrder(id, reason, email));
    }

    @PutMapping("/{id}/serve-all")
    public ResponseEntity<OrderResponseDTO> serveAll(@PathVariable Long id, Authentication auth) {
        String email = authUtils.getUserEmail(auth);
        return ResponseEntity.ok(orderService.serveAllItems(id, email));
    }

}
