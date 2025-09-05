package pos.pos.Repository.Menu;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pos.pos.Entity.Menu.ItemVariant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ItemVariantRepository extends JpaRepository<ItemVariant, Long> {

    List<ItemVariant> findByItem_IdOrderBySortOrderAscIdAsc(Long itemId);

    Optional<ItemVariant> findByIdAndItem_Id(Long id, Long itemId);

    boolean existsByItem_IdAndNameIgnoreCase(Long itemId, String name);

    long countByItem_Id(Long itemId);

    @Modifying
    @Query("update ItemVariant v set v.isDefault = false where v.item.id = :itemId")
    void clearDefaultForItem(@Param("itemId") Long itemId);

    Optional<ItemVariant> findByPublicId(UUID publicId);

    Optional<ItemVariant> findByItem_IdAndSortOrder(Long itemId, Integer sortOrder);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ItemVariant v SET v.sortOrder = :pos WHERE v.item.id = :itemId AND v.id = :variantId")
    void updateSortOrder(@Param("itemId") Long itemId,
                         @Param("variantId") Long variantId,
                         @Param("pos") int pos);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ItemVariant v SET v.sortOrder = v.sortOrder + 1 " +
            "WHERE v.item.id = :itemId AND v.sortOrder >= :fromPos")
    void shiftRightFrom(@Param("itemId") Long itemId, @Param("fromPos") int fromPos);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE ItemVariant v SET v.sortOrder = v.sortOrder - 1 " +
            "WHERE v.item.id = :itemId AND v.sortOrder > :fromPos")
    void shiftLeftAfter(@Param("itemId") Long itemId, @Param("fromPos") int fromPos);


//    List<ItemVariant> findByItem_PublicIdOrderBySortOrderAscIdAsc(UUID itemPublicId);
//
//    Optional<ItemVariant> findByPublicIdAndItem_PublicId(UUID variantPublicId, UUID itemPublicId);
//
//    boolean existsByItem_PublicIdAndNameIgnoreCase(UUID itemPublicId, String name);
//
//    long countByItem_PublicId(UUID itemPublicId);
//
//    @Modifying
//    @Query("update ItemVariant v set v.isDefault = false where v.item.publicId = :itemPublicId")
//    void clearDefaultForItemPublicId(@Param("itemPublicId") UUID itemPublicId);
}
