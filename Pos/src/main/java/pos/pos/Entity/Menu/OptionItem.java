package pos.pos.Entity.Menu;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OptionItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private BigDecimal priceDelta = BigDecimal.ZERO;
    private Integer sortOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    private OptionGroup group;
}
