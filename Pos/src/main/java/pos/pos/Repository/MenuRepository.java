package pos.pos.Repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pos.pos.Entity.Menu.Menu;

import java.util.Optional;


public interface MenuRepository extends JpaRepository<Menu, Long> {

    @EntityGraph(attributePaths = {
            "sections",
            "sections.items",
            "sections.items.variants",
            "sections.items.optionGroups",
            "sections.items.optionGroups.options"
    })
    Optional<Menu> findWithTreeById(Long id);
}
