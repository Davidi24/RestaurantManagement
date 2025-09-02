package pos.pos.Controller.Order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderCreateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderResponseDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderStatusUpdateDTO;
import pos.pos.DTO.Order.OrderCollectorDTO.OrderUpdateDTO;
import pos.pos.Service.Interfecaes.OrderService;


import java.util.List;

//TODO: Make a get orders, with pagination and date filter

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderCreateDTO dto, Authentication authentication) {
        JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
        String email = jwtAuth.getToken().getSubject();
        return ResponseEntity.ok(orderService.createOrder(dto, email));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderResponseDTO> updateOrder(@PathVariable Long id, @RequestBody OrderUpdateDTO dto) {
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
    public ResponseEntity<OrderResponseDTO> updateStatus(@PathVariable Long id, @RequestBody OrderStatusUpdateDTO dto) {
        return ResponseEntity.ok(orderService.updateStatus(id, dto));
    }
}
