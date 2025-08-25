package pos.pos.Entity.Menu;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class MenuItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private BigDecimal basePrice = BigDecimal.ZERO;
    private boolean available = true;
    private Integer sortOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    private MenuSection section;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    @Builder.Default
    private Set<ItemVariant> variants = new LinkedHashSet<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    @Builder.Default
    private Set<OptionGroup> optionGroups = new LinkedHashSet<>();
}
