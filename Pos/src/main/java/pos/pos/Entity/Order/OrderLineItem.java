package pos.pos.Entity.Order;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OrderLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long menuItemId;
    private String itemName;
    private Double unitPrice;

    private Integer quantity;

    @Enumerated(EnumType.STRING)
    private FulfillmentStatus fulfillmentStatus;

    private Double lineSubtotal;
    private Double lineDiscount;
    private Double lineGrandTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @OneToOne(mappedBy = "lineItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrderVariantSnapshot variantSnapshot;

    @OneToMany(mappedBy = "lineItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderOptionSnapshot> optionSnapshots = new ArrayList<>();

    @OneToMany(mappedBy = "lineItem", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderDiscount> discounts = new ArrayList<>();
}
