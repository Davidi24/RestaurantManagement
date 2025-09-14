package pos.pos.Controller.KDS;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pos.pos.Config.ApiPaths;
import pos.pos.Entity.Order.FulfillmentStatus;
import pos.pos.Service.Interfecaes.KDS.KdsService;

@RestController
@RequestMapping(value = ApiPaths.Kds.BASE, produces = "application/json")
@RequiredArgsConstructor
public class KdsController {

    private final KdsService kdsService;

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN', 'KITCHEN')")
    @PatchMapping("/orders/{orderId}/items/{lineItemId}/status")
    public ResponseEntity<Void> updateItemStatus(
            @PathVariable Long orderId,
            @PathVariable Long lineItemId,
            @RequestParam FulfillmentStatus status,
            @RequestParam String userEmail) {
        kdsService.updateItemStatus(orderId, lineItemId, status, userEmail);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN', 'KITCHEN')")
    @PatchMapping("/orders/{orderId}/ready")
    public ResponseEntity<Void> markTicketReady(
            @PathVariable Long orderId,
            @RequestParam String userEmail) {
        kdsService.markTicketReady(orderId, userEmail);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN', 'KITCHEN')")
    @PatchMapping("/orders/{orderId}/bump")
    public ResponseEntity<Void> bumpTicket(
            @PathVariable Long orderId,
            @RequestParam String userEmail) {
        kdsService.bumpTicket(orderId, userEmail);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN', 'KITCHEN')")
    @PatchMapping("/orders/{orderId}/recall")
    public ResponseEntity<Void> recallTicket(
            @PathVariable Long orderId,
            @RequestParam String userEmail) {
        kdsService.recallTicket(orderId, userEmail);
        return ResponseEntity.ok().build();
    }
}
