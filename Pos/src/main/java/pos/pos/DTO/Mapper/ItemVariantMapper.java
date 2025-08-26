package pos.pos.DTO.Mapper;

import org.springframework.stereotype.Component;
import pos.pos.DTO.*;
import pos.pos.DTO.Menu.ItemVariantCreateRequest;
import pos.pos.DTO.Menu.ItemVariantResponse;
import pos.pos.DTO.Menu.ItemVariantUpdateRequest;
import pos.pos.Entity.Menu.ItemVariant;
import pos.pos.Entity.Menu.MenuItem;

@Component
public class ItemVariantMapper {

    public ItemVariantResponse toResponse(ItemVariant entity) {
        if (entity == null) return null;

        return new ItemVariantResponse(
                entity.getId(),
                entity.getName(),
                entity.getPriceOverride(),
                entity.isDefault(),
                entity.getSortOrder(),
                entity.getItem() != null ? entity.getItem().getId() : null
        );
    }

    public ItemVariant toEntity(ItemVariantCreateRequest request, MenuItem parentItem) {
        if (request == null) return null;

        return ItemVariant.builder()
                .name(request.name())
                .priceOverride(request.priceOverride())
                .isDefault(Boolean.TRUE.equals(request.isDefault())) // null → false
                .sortOrder(request.sortOrder() != null ? request.sortOrder() : 0)
                .item(parentItem)
                .build();
    }

    public void updateEntity(ItemVariant entity, ItemVariantUpdateRequest request) {
        if (request == null) return;

        if (request.name() != null) {
            entity.setName(request.name());
        }
        if (request.priceOverride() != null) {
            entity.setPriceOverride(request.priceOverride());
        }
        if (request.isDefault() != null) {
            entity.setDefault(request.isDefault());
        }
        if (request.sortOrder() != null) {
            entity.setSortOrder(request.sortOrder());
        }
    }
}
