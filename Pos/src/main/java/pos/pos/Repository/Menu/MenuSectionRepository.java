package pos.pos.Repository.Menu;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pos.pos.Entity.Menu.MenuSection;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuSectionRepository extends JpaRepository<MenuSection, Long> {

    List<MenuSection> findByMenu_IdOrderByOrderKeyAsc(Long menuId);
    List<MenuSection> findByMenu_IdOrderByOrderKeyAsc(Long menuId, Pageable pageable);
    List<MenuSection> findByMenu_IdAndIdNotOrderByOrderKeyAsc(Long menuId, Long excludeId, Pageable pageable);

    Optional<MenuSection> findFirstByMenu_IdOrderByOrderKeyAsc(Long menuId);
    Optional<MenuSection> findFirstByMenu_IdOrderByOrderKeyDesc(Long menuId);

    Optional<MenuSection> findByIdAndMenu_Id(Long id, Long menuId);

    boolean existsByMenu_IdAndNameIgnoreCase(Long menuId, String name);

    long countByMenu_Id(Long menuId);

    @Query("""
           SELECT COUNT(ms) FROM MenuSection ms
           WHERE ms.menu.id = :menuId AND ms.orderKey < :orderKey
           """)
    long countBefore(@Param("menuId") Long menuId, @Param("orderKey") BigDecimal orderKey);

    @Modifying
    @Query(value = """
      WITH ranked AS (
        SELECT id, (ROW_NUMBER() OVER (PARTITION BY menu_id ORDER BY order_key))*1000 AS rk
        FROM menu_section
        WHERE menu_id = :menuId
      )
      UPDATE menu_section m
      SET order_key = ranked.rk
      FROM ranked
      WHERE ranked.id = m.id
    """, nativeQuery = true)
    int rebalance(@Param("menuId") Long menuId);


    List<MenuSection> findByMenu_PublicIdOrderByOrderKeyAsc(UUID menuPublicId);
    List<MenuSection> findByMenu_PublicIdOrderByOrderKeyAsc(UUID menuPublicId, Pageable pageable);
    List<MenuSection> findByMenu_PublicIdAndIdNotOrderByOrderKeyAsc(UUID menuPublicId, Long excludeId, Pageable pageable);

    Optional<MenuSection> findFirstByMenu_PublicIdOrderByOrderKeyAsc(UUID menuPublicId);
    Optional<MenuSection> findFirstByMenu_PublicIdOrderByOrderKeyDesc(UUID menuPublicId);

    Optional<MenuSection> findByPublicId(UUID publicId);
    Optional<MenuSection> findByPublicIdAndMenu_PublicId(UUID sectionPublicId, UUID menuPublicId);

    boolean existsByMenu_PublicIdAndNameIgnoreCase(UUID menuPublicId, String name);

    long countByMenu_PublicId(UUID menuPublicId);

    @Query("""
       SELECT COUNT(ms) FROM MenuSection ms
       WHERE ms.menu.publicId = :menuPublicId AND ms.orderKey < :orderKey
       """)
    long countBefore(@Param("menuPublicId") UUID menuPublicId, @Param("orderKey") BigDecimal orderKey);
}
