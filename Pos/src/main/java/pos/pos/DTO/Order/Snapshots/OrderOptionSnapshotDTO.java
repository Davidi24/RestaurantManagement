package pos.pos.DTO.Order.Snapshots;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderOptionSnapshotDTO {
    private Long optionId;
    private String optionName;
    private Double priceDelta;
}
