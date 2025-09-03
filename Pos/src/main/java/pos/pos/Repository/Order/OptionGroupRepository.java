package pos.pos.Repository.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import pos.pos.Entity.Menu.OptionGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OptionGroupRepository extends JpaRepository<OptionGroup, Long> {

    List<OptionGroup> findByItem_IdOrderBySortOrderAscIdAsc(Long itemId);

    Optional<OptionGroup> findByIdAndItem_Id(Long id, Long itemId);

    boolean existsByItem_IdAndNameIgnoreCase(Long itemId, String name);

    long countByItem_Id(Long itemId);

    Optional<OptionGroup> findByPublicId(UUID publicId);

    List<OptionGroup> findByItem_PublicIdOrderBySortOrderAscIdAsc(UUID itemPublicId);

    Optional<OptionGroup> findByPublicIdAndItem_PublicId(UUID groupPublicId, UUID itemPublicId);

    boolean existsByItem_PublicIdAndNameIgnoreCase(UUID itemPublicId, String name);

    long countByItem_PublicId(UUID itemPublicId);
}
