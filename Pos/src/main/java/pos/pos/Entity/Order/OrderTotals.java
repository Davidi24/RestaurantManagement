package pos.pos.Entity.Order;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderTotals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double itemsSubtotal;
    private Double discountTotal;
    private Double serviceChargeTotal;
    private Double grandTotal;
    private Double paidTotal;
    private Double balanceDue;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;
}
