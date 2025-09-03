package pos.pos.Repository.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import pos.pos.Entity.Menu.OptionItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OptionItemRepository extends JpaRepository<OptionItem, Long> {

    List<OptionItem> findByGroup_IdOrderBySortOrderAscIdAsc(Long groupId);

    Optional<OptionItem> findByIdAndGroup_Id(Long id, Long groupId);

    boolean existsByGroup_IdAndNameIgnoreCase(Long groupId, String name);

    long countByGroup_Id(Long groupId);

    Optional<OptionItem> findByPublicId(UUID publicId);

    List<OptionItem> findByGroup_PublicIdOrderBySortOrderAscIdAsc(UUID groupPublicId);

    Optional<OptionItem> findByPublicIdAndGroup_PublicId(UUID optionPublicId, UUID groupPublicId);

    boolean existsByGroup_PublicIdAndNameIgnoreCase(UUID groupPublicId, String name);

    long countByGroup_PublicId(UUID groupPublicId);
}
