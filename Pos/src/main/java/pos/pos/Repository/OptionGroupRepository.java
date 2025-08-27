package pos.pos.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pos.pos.Entity.Menu.OptionGroup;

import java.util.List;
import java.util.Optional;

public interface OptionGroupRepository extends JpaRepository<OptionGroup, Long> {

    List<OptionGroup> findByItem_IdOrderBySortOrderAscIdAsc(Long itemId);

    Optional<OptionGroup> findByIdAndItem_Id(Long id, Long itemId);

    boolean existsByItem_IdAndNameIgnoreCase(Long itemId, String name);

    long countByItem_Id(Long itemId);
}
