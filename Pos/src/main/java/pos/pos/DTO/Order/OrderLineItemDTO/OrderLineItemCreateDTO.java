package pos.pos.DTO.Order.OrderLineItemDTO;

import lombok.*;
import pos.pos.DTO.Order.Snapshots.OrderOptionSnapshotDTO;
import pos.pos.DTO.Order.Snapshots.OrderVariantSnapshotDTO;

import java.util.List;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderLineItemCreateDTO {
    private Long menuItemId;
    private String itemName;
    private Double unitPrice;
    private Integer quantity;
    private OrderVariantSnapshotDTO variantSnapshot;
    private List<OrderOptionSnapshotDTO> optionSnapshots;
}
