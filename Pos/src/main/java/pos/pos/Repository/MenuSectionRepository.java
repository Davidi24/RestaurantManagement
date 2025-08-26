package pos.pos.Repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pos.pos.Entity.Menu.MenuSection;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface MenuSectionRepository extends JpaRepository<MenuSection, Long> {

    List<MenuSection> findByMenu_IdOrderByOrderKeyAsc(Long menuId);
    List<MenuSection> findByMenu_IdOrderByOrderKeyAsc(Long menuId, Pageable pageable);
    List<MenuSection> findByMenu_IdAndIdNotOrderByOrderKeyAsc(Long menuId, Long excludeId, Pageable pageable);

    Optional<MenuSection> findFirstByMenu_IdOrderByOrderKeyAsc(Long menuId);
    Optional<MenuSection> findFirstByMenu_IdOrderByOrderKeyDesc(Long menuId);

    Optional<MenuSection> findByIdAndMenu_Id(Long id, Long menuId);

    boolean existsByMenu_IdAndNameIgnoreCase(Long menuId, String name);

    long countByMenu_Id(Long menuId);

    // 0-based index of the entity by counting how many come before its orderKey
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
}
