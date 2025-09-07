package pos.pos.DTO.Order.OrderCollectorDTO;

import lombok.*;
import pos.pos.DTO.Order.OrderLineItemDTO.OrderLineItemResponseDTO;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderResponseDTO {
    private Long id;
    private String orderNumber;
    private String status;
    private String type;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private String userEmail;
    private Long tableId;
    private Long customerId;
    private String notes;
    private List<OrderLineItemResponseDTO> lineItems;
    private Double grandTotal;
    private Double paidTotal;
    private Double balanceDue;
    private Long numberOfGuests;
}
