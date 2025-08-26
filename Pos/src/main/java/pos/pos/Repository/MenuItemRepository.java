package pos.pos.Repository;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pos.pos.Entity.Menu.MenuItem;

import java.util.List;
import java.util.Optional;

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
}
