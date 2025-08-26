package pos.pos.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pos.pos.Entity.Menu.MenuSection;

import java.util.List;
import java.util.Optional;

public interface MenuSectionRepository extends JpaRepository<MenuSection, Long> {
    List<MenuSection> findByMenu_IdOrderBySortOrderAscIdAsc(Long menuId);
    boolean existsByMenu_IdAndNameIgnoreCase(Long menuId, String name);
    Optional<MenuSection> findByIdAndMenu_Id(Long id, Long menuId);
}
