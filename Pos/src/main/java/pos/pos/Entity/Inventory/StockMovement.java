package pos.pos.Entity.Inventory;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "stock_movement",
       indexes = {
         @Index(name="ix_stock_movement_item_time", columnList = "inventory_item_id, occurredAt"),
         @Index(name="ix_stock_movement_type", columnList = "type")
       })
@SequenceGenerator(name="stock_movement_seq", sequenceName="stock_movement_seq", allocationSize=50)
public class StockMovement {

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="stock_movement_seq")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="inventory_item_id", nullable=false)
    private InventoryItem inventoryItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private MovementType type;

    @Column(nullable=false, precision=19, scale=6)
    private BigDecimal quantity;

    @Column(length=256)
    private String reason;

    @Column(length=128)
    private String reference;

    @CreationTimestamp
    @Column(nullable=false, updatable=false)
    private OffsetDateTime occurredAt;

    @Column(length=64)
    private String createdBy;
}