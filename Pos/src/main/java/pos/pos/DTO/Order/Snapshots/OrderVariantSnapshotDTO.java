package pos.pos.DTO.Order.Snapshots;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderVariantSnapshotDTO {
    private Long variantId;
    private String variantName;
    private Double priceOverride;
}
