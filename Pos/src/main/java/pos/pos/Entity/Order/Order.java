package pos.pos.Entity.Order;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "orders",
        indexes = {
                @Index(name = "ix_orders_order_number", columnList = "orderNumber")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_orders_order_number", columnNames = {"orderNumber"})
        }
)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
    @SequenceGenerator(name = "order_seq", sequenceName = "order_seq", allocationSize = 50)
    private Long id;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    private OrderType type;

    private LocalDateTime openedAt;
    private LocalDateTime closedAt;

    private String userEmail;
    private Long tableId;
    private Long customerId;

    private String notes;
    private Long numberOfGuests;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderLineItem> lineItems = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderDiscount> discounts = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private OrderTotals totals;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderEvent> events = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (openedAt == null) openedAt = LocalDateTime.now();
        if (status == null) status = OrderStatus.OPEN;
        if (type == null) type = OrderType.DINE_IN;
    }
}
