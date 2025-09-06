package pos.pos.Repository.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    Optional<OptionGroup> findByItem_IdAndSortOrder(Long itemId, Integer sortOrder);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OptionGroup g SET g.sortOrder = :pos WHERE g.item.id = :itemId AND g.id = :groupId")
    void updateSortOrder(@Param("itemId") Long itemId,
                         @Param("groupId") Long groupId,
                         @Param("pos") int pos);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OptionGroup g SET g.sortOrder = g.sortOrder + 1 " +
            "WHERE g.item.id = :itemId AND g.sortOrder >= :fromPos")
    void shiftRightFrom(@Param("itemId") Long itemId, @Param("fromPos") int fromPos);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE OptionGroup g SET g.sortOrder = g.sortOrder - 1 " +
            "WHERE g.item.id = :itemId AND g.sortOrder > :fromPos")
    void shiftLeftAfter(@Param("itemId") Long itemId, @Param("fromPos") int fromPos);
}
