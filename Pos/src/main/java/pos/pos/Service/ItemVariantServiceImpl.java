package pos.pos.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pos.pos.DTO.Mapper.ItemVariantMapper;
import pos.pos.DTO.Menu.ItemVariantCreateRequest;
import pos.pos.DTO.Menu.ItemVariantResponse;
import pos.pos.DTO.Menu.ItemVariantUpdateRequest;
import pos.pos.Entity.Menu.ItemVariant;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Exeption.AlreadyExistsException;
import pos.pos.Exeption.MenuItemExeption;
import pos.pos.Exeption.MenuNotFoundException;
import pos.pos.Exeption.MenuSectionNotFound;
import pos.pos.Repository.ItemVariantRepository;
import pos.pos.Repository.MenuItemRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemVariantServiceImpl implements pos.pos.Service.Interfecaes.ItemVariantService {

    private final MenuItemRepository itemRepository;
    private final ItemVariantRepository variantRepository;
    private final ItemVariantMapper mapper;

    /** Ensures the path (menuId -> sectionId -> itemId) is consistent and returns the MenuItem */
    private MenuItem loadItemOrThrow(Long menuId, Long sectionId, Long itemId) {
        MenuItem item = itemRepository.findByIdAndSection_Id(itemId, sectionId)
                .orElseThrow(() -> new MenuSectionNotFound(menuId, sectionId));
        if (item.getSection() == null || item.getSection().getMenu() == null || !item.getSection().getMenu().getId().equals(menuId)) {
            throw new MenuItemExeption(menuId, sectionId, itemId);
        }
        return item;
    }

    @Override
    @Transactional
    public List<ItemVariantResponse> listVariants(Long menuId, Long sectionId, Long itemId) {
        loadItemOrThrow(menuId, sectionId, itemId);
        return variantRepository.findByItem_IdOrderBySortOrderAscIdAsc(itemId)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public ItemVariantResponse getVariant(Long menuId, Long sectionId, Long itemId, Long variantId) {
        loadItemOrThrow(menuId, sectionId, itemId);
        ItemVariant variant = variantRepository.findByIdAndItem_Id(variantId, itemId)
                .orElseThrow(() -> new MenuItemExeption(menuId, sectionId, itemId));
        return mapper.toResponse(variant);
    }

    @Override
    @Transactional
    public ItemVariantResponse createVariant(Long menuId, Long sectionId, Long itemId, ItemVariantCreateRequest request) {
        MenuItem item = loadItemOrThrow(menuId, sectionId, itemId);

        // Unique name per item (case-insensitive)
        if (request.name() != null && variantRepository.existsByItem_IdAndNameIgnoreCase(itemId, request.name())) {
            throw new AlreadyExistsException("Menu variant ", request.name());
        }

        ItemVariant entity = mapper.toEntity(request, item);
        if (entity.isDefault()) {
            variantRepository.clearDefaultForItem(itemId);
        }

        if (entity.getSortOrder() == null) {
            long count = variantRepository.countByItem_Id(itemId);
            entity.setSortOrder((int) count);
        }

        ItemVariant saved = variantRepository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ItemVariantResponse updateVariant(Long menuId, Long sectionId, Long itemId, Long variantId, ItemVariantUpdateRequest request) {
        loadItemOrThrow(menuId, sectionId, itemId);

        ItemVariant entity = variantRepository.findByIdAndItem_Id(variantId, itemId)
                .orElseThrow(() -> new MenuItemExeption(menuId, sectionId, itemId));

        // Name uniqueness (only when changing)
        if (request.name() != null && !request.name().equalsIgnoreCase(entity.getName())
                && variantRepository.existsByItem_IdAndNameIgnoreCase(itemId, request.name())) {
            throw new AlreadyExistsException("Menu variant ", request.name());
        }

        boolean willBeDefault = Boolean.TRUE.equals(request.isDefault());
        if (willBeDefault) {
            variantRepository.clearDefaultForItem(itemId);
        }

        // Apply field updates
        mapper.updateEntity(entity, request);

        ItemVariant saved = variantRepository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteVariant(Long menuId, Long sectionId, Long itemId, Long variantId) {
        loadItemOrThrow(menuId, sectionId, itemId);

        ItemVariant entity = variantRepository.findByIdAndItem_Id(variantId, itemId)
                .orElseThrow(() -> new MenuItemExeption(menuId, sectionId, itemId));

        variantRepository.delete(entity);
    }
}
