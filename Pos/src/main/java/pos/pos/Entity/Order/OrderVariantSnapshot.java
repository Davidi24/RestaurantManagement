package pos.pos.Entity.Order;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderVariantSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long variantId;
    private String variantName;
    private Double priceOverride;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_item_id")
    private OrderLineItem lineItem;
}
