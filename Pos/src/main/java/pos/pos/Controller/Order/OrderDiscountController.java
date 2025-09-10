package pos.pos.Controller.Order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pos.pos.Config.Security.AuthUtils;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountCreateDTO;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountResponseDTO;
import pos.pos.DTO.Order.OrderDiscount.OrderDiscountUpdateDTO;
import pos.pos.Service.Interfecaes.OrderDiscountService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/orders/{orderId}/discounts", produces = "application/json")
@RequiredArgsConstructor
public class OrderDiscountController {

    private final OrderDiscountService orderDiscountService;
    private final AuthUtils authUtils;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    public ResponseEntity<OrderDiscountResponseDTO> addDiscount(
            @PathVariable Long orderId,
            @Valid @RequestBody OrderDiscountCreateDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(orderDiscountService.addDiscount(orderId, dto, authUtils.getUserEmail(authentication)));
    }

    @PutMapping("/{discountId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    public ResponseEntity<OrderDiscountResponseDTO> updateDiscount(
            @PathVariable Long orderId,
            @PathVariable Long discountId,
            @Valid @RequestBody OrderDiscountUpdateDTO dto,
            Authentication authentication) {
        dto.setId(discountId);
        return ResponseEntity.ok(orderDiscountService.updateDiscount(orderId, dto, authUtils.getUserEmail(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<OrderDiscountResponseDTO>> getDiscounts(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderDiscountService.getDiscounts(orderId));
    }

    @GetMapping("/{discountId}")
    public ResponseEntity<OrderDiscountResponseDTO> getDiscountById(
            @PathVariable Long orderId,
            @PathVariable Long discountId) {
        return ResponseEntity.ok(orderDiscountService.getDiscountById(orderId, discountId));
    }

    @DeleteMapping("/{discountId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    public ResponseEntity<Void> removeDiscount(
            @PathVariable Long orderId,
            @PathVariable Long discountId,
            Authentication authentication) {
        orderDiscountService.removeDiscount(orderId, discountId, authUtils.getUserEmail(authentication));
        return ResponseEntity.noContent().build();
    }
}
