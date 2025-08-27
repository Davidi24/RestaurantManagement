package pos.pos.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pos.pos.Entity.Menu.OptionItem;

import java.util.List;
import java.util.Optional;

public interface OptionItemRepository extends JpaRepository<OptionItem, Long> {

    List<OptionItem> findByGroup_IdOrderBySortOrderAscIdAsc(Long groupId);

    Optional<OptionItem> findByIdAndGroup_Id(Long id, Long groupId);

    boolean existsByGroup_IdAndNameIgnoreCase(Long groupId, String name);

    long countByGroup_Id(Long groupId);
}
