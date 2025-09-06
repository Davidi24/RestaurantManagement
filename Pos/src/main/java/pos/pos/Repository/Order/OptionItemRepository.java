package pos.pos.Repository.Order;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
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

    Optional<OptionItem> findByGroup_IdAndSortOrder(Long groupId, Integer sortOrder);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OptionItem o SET o.sortOrder = :pos WHERE o.group.id = :groupId AND o.id = :optionId")
    void updateSortOrder(@Param("groupId") Long groupId,
                         @Param("optionId") Long optionId,
                         @Param("pos") int pos);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OptionItem o SET o.sortOrder = o.sortOrder + 1 " +
            "WHERE o.group.id = :groupId AND o.sortOrder >= :fromPos")
    void shiftRightFrom(@Param("groupId") Long groupId, @Param("fromPos") int fromPos);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OptionItem o SET o.sortOrder = o.sortOrder - 1 " +
            "WHERE o.group.id = :groupId AND o.sortOrder > :fromPos")
    void shiftLeftAfter(@Param("groupId") Long groupId, @Param("fromPos") int fromPos);
}
