package pos.pos.Entity.Menu;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID publicId;

    private String name;
    private String description;

    @OneToMany(
            mappedBy = "menu",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<MenuSection> sections = new ArrayList<>();

    @PrePersist
    private void ensurePublicId() {
        if (publicId == null) {
            publicId = UUID.randomUUID();
        }
    }
}
