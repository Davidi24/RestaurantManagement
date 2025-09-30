package pos.pos.Entity.Order;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderOptionSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long optionId;

    private UUID optionPublicId;

    private String optionName;
    private Double priceDelta;

    @Builder.Default
    private Integer selectionCount = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_item_id")
    private OrderLineItem lineItem;

    @PrePersist
    void prePersist() {
        if (selectionCount == null || selectionCount <= 0) selectionCount = 1;
    }
}
