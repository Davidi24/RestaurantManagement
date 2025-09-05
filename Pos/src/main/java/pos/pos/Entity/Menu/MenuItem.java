package pos.pos.Entity.Menu;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.*;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(
        name = "menu_item",
        indexes = {
                @Index(name = "ix_menu_item_sectionid", columnList = "section_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_menu_item_sectionid_sortorder", columnNames = {"section_id", "sortOrder"}),
                @UniqueConstraint(name = "uq_menu_item_sectionid_name", columnNames = {"section_id", "name"})
        }
)
public class MenuItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    private String name;

    @Builder.Default
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Builder.Default
    private boolean available = true;

    @Builder.Default
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

    @PrePersist
    private void ensurePublicId() {
        if (publicId == null) {
            publicId = UUID.randomUUID();
        }
    }
}
