package pos.pos.Controller.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pos.pos.DTO.Order.OrderEventResponseDTO;
import pos.pos.Service.Interfecaes.OrderEventService;

import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/events")
@RequiredArgsConstructor
public class OrderEventController {

    private final OrderEventService orderEventService;

    @GetMapping
    public ResponseEntity<List<OrderEventResponseDTO>> getEvents(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderEventService.getEvents(orderId));
    }
}
