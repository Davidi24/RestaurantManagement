package pos.pos.Repository;

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

    List<ItemVariant> findByItem_PublicIdOrderBySortOrderAscIdAsc(UUID itemPublicId);

    Optional<ItemVariant> findByPublicIdAndItem_PublicId(UUID variantPublicId, UUID itemPublicId);

    boolean existsByItem_PublicIdAndNameIgnoreCase(UUID itemPublicId, String name);

    long countByItem_PublicId(UUID itemPublicId);

    @Modifying
    @Query("update ItemVariant v set v.isDefault = false where v.item.publicId = :itemPublicId")
    void clearDefaultForItemPublicId(@Param("itemPublicId") UUID itemPublicId);
}
