package pos.pos.Entity.Menu;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class OptionGroup {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private OptionGroupType type = OptionGroupType.SINGLE;

    private boolean required = false;
    private Integer minSelections;
    private Integer maxSelections;
    private Integer sortOrder = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    private MenuItem item;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC, id ASC")
    @Builder.Default
    private Set<OptionItem> options = new LinkedHashSet<>();
}
