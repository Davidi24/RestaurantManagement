package pos.pos.Repository.Menu;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pos.pos.Entity.Menu.MenuItem;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findBySection_IdOrderBySortOrderAscIdAsc(Long sectionId);

    Optional<MenuItem> findByIdAndSection_Id(Long id, Long sectionId);

    boolean existsBySection_IdAndNameIgnoreCase(Long sectionId, String name);

    long countBySection_Id(Long sectionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE MenuItem mi SET mi.sortOrder = mi.sortOrder + 1 " +
            "WHERE mi.section.id = :sectionId AND mi.sortOrder >= :fromPos")
    void shiftRightFrom(@Param("sectionId") Long sectionId, @Param("fromPos") int fromPos);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE MenuItem mi SET mi.sortOrder = mi.sortOrder - 1 " +
            "WHERE mi.section.id = :sectionId AND mi.sortOrder > :fromPos")
    void shiftLeftAfter(@Param("sectionId") Long sectionId, @Param("fromPos") int fromPos);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE MenuItem mi SET mi.sortOrder = mi.sortOrder + 1 " +
            "WHERE mi.section.id = :sectionId AND mi.sortOrder >= :startPos AND mi.sortOrder < :endPos")
    void shiftRightRange(@Param("sectionId") Long sectionId,
                         @Param("startPos") int startPos,
                         @Param("endPos") int endPos);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE MenuItem mi SET mi.sortOrder = mi.sortOrder - 1 " +
            "WHERE mi.section.id = :sectionId AND mi.sortOrder > :startPos AND mi.sortOrder <= :endPos")
    void shiftLeftRange(@Param("sectionId") Long sectionId,
                        @Param("startPos") int startPos,
                        @Param("endPos") int endPos);

    Optional<MenuItem> findByPublicId(UUID publicId);

    List<MenuItem> findBySection_PublicIdOrderBySortOrderAscIdAsc(UUID sectionPublicId);

    Optional<MenuItem> findByPublicIdAndSection_PublicId(UUID itemPublicId, UUID sectionPublicId);

    boolean existsBySection_PublicIdAndNameIgnoreCase(UUID sectionPublicId, String name);

    Optional<MenuItem> findBySection_IdAndSortOrder(Long sectionId, Integer sortOrder);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE menu_item
            SET sort_order = CASE
                WHEN id = :idA THEN :posB
                WHEN id = :idB THEN :posA
                ELSE sort_order
            END
            WHERE section_id = :sectionId
              AND id IN (:idA, :idB)
            """, nativeQuery = true)

    void swapSortOrders(@Param("sectionId") Long sectionId,
                        @Param("idA") Long idA, @Param("posA") int posA,
                        @Param("idB") Long idB, @Param("posB") int posB);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE MenuItem mi SET mi.sortOrder = :pos WHERE mi.section.id = :sectionId AND mi.id = :itemId")
    void updateSortOrder(@Param("sectionId") Long sectionId,
                        @Param("itemId") Long itemId,
                        @Param("pos") int pos);
}
