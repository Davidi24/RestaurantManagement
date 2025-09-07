package pos.pos.Entity.Order;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(
  name = "order_number_counters",
  uniqueConstraints = @UniqueConstraint(name = "uk_counter_date_table", columnNames = {"counter_date", "table_id"})
)
public class OrderNumberCounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "counter_date", nullable = false)
    private LocalDate date;

    @Column(name = "table_id", nullable = false)
    private Long tableId;

    @Column(nullable = false)
    private Long value;

    @Version
    private Long version;
}
