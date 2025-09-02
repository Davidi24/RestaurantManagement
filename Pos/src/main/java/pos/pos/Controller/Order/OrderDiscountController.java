package pos.pos.Controller.Order;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountCreateDTO;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountResponseDTO;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountUpdateDTO;
import pos.pos.Service.Interfecaes.OrderDiscountService;

import java.util.List;

@RestController
@RequestMapping("/api/orders/{orderId}/discounts")
@RequiredArgsConstructor
public class OrderDiscountController {

    private final OrderDiscountService orderDiscountService;

    @PostMapping
    public ResponseEntity<OrderDiscountResponseDTO> addDiscount(@PathVariable Long orderId, @RequestBody OrderDiscountCreateDTO dto) {
        return ResponseEntity.ok(orderDiscountService.addDiscount(orderId, dto));
    }

    @PutMapping("/{discountId}")
    public ResponseEntity<OrderDiscountResponseDTO> updateDiscount(@PathVariable Long orderId, @PathVariable Long discountId, @RequestBody OrderDiscountUpdateDTO dto) {
        dto.setId(discountId);
        return ResponseEntity.ok(orderDiscountService.updateDiscount(orderId, dto));
    }

    @GetMapping
    public ResponseEntity<List<OrderDiscountResponseDTO>> getDiscounts(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderDiscountService.getDiscounts(orderId));
    }

    @GetMapping("/{discountId}")
    public ResponseEntity<OrderDiscountResponseDTO> getDiscountById(@PathVariable Long orderId, @PathVariable Long discountId) {
        return ResponseEntity.ok(orderDiscountService.getDiscountById(orderId, discountId));
    }

    @DeleteMapping("/{discountId}")
    public ResponseEntity<Void> removeDiscount(@PathVariable Long orderId, @PathVariable Long discountId) {
        orderDiscountService.removeDiscount(orderId, discountId);
        return ResponseEntity.noContent().build();
    }
}
