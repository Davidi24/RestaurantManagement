package pos.pos.Repository.Menu;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pos.pos.Entity.Menu.Menu;

import java.util.Optional;
import java.util.UUID;


public interface MenuRepository extends JpaRepository<Menu, Long> {

    @EntityGraph(attributePaths = {
            "sections",
            "sections.items",
            "sections.items.variants",
            "sections.items.optionGroups",
            "sections.items.optionGroups.options"
    })
    Optional<Menu> findWithTreeById(Long id);

    Optional<Menu> findByPublicId(UUID publicId);

    @EntityGraph(attributePaths = {
            "sections",
            "sections.items",
            "sections.items.variants",
            "sections.items.optionGroups",
            "sections.items.optionGroups.options"
    })
    Optional<Menu> findWithTreeByPublicId(UUID publicId);
}
