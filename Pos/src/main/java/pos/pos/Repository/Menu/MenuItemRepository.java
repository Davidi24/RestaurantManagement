package pos.pos.Repository.Menu;

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

    @Modifying
    @Query("UPDATE MenuItem mi SET mi.sortOrder = mi.sortOrder + 1 " +
            "WHERE mi.section.id = :sectionId AND mi.sortOrder >= :fromPos")
    int shiftRightFrom(@Param("sectionId") Long sectionId, @Param("fromPos") int fromPos);

    @Modifying
    @Query("UPDATE MenuItem mi SET mi.sortOrder = mi.sortOrder - 1 " +
            "WHERE mi.section.id = :sectionId AND mi.sortOrder > :fromPos")
    int shiftLeftAfter(@Param("sectionId") Long sectionId, @Param("fromPos") int fromPos);

    @Modifying
    @Query("UPDATE MenuItem mi SET mi.sortOrder = mi.sortOrder + 1 " +
            "WHERE mi.section.id = :sectionId AND mi.sortOrder >= :startPos AND mi.sortOrder < :endPos")
    int shiftRightRange(@Param("sectionId") Long sectionId,
                        @Param("startPos") int startPos,
                        @Param("endPos") int endPos);

    @Modifying
    @Query("UPDATE MenuItem mi SET mi.sortOrder = mi.sortOrder - 1 " +
            "WHERE mi.section.id = :sectionId AND mi.sortOrder > :startPos AND mi.sortOrder <= :endPos")
    int shiftLeftRange(@Param("sectionId") Long sectionId,
                       @Param("startPos") int startPos,
                       @Param("endPos") int endPos);

    Optional<MenuItem> findByPublicId(UUID publicId);

    List<MenuItem> findBySection_PublicIdOrderBySortOrderAscIdAsc(UUID sectionPublicId);

    Optional<MenuItem> findByPublicIdAndSection_PublicId(UUID itemPublicId, UUID sectionPublicId);

    boolean existsBySection_PublicIdAndNameIgnoreCase(UUID sectionPublicId, String name);

}
