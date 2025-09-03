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

    // legacy (keep during transition)
    private Long optionId;

    // new global identifier
    private UUID optionPublicId;

    private String optionName;
    private Double priceDelta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_item_id")
    private OrderLineItem lineItem;
}
