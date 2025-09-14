package pos.pos.Service.Menu;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pos.pos.DTO.Mapper.MenuMapper.ItemVariantMapper;
import pos.pos.DTO.Menu.VariantDTO.ItemVariantCreateRequest;
import pos.pos.DTO.Menu.VariantDTO.ItemVariantResponse;
import pos.pos.DTO.Menu.VariantDTO.ItemVariantUpdateRequest;
import pos.pos.Entity.Menu.ItemVariant;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Exeption.AlreadyExistsException;
import pos.pos.Exeption.ItemVariantNotFound;
import pos.pos.Exeption.MenuItemException;
import pos.pos.Exeption.MenuSectionNotFound;
import pos.pos.Repository.Menu.ItemVariantRepository;
import pos.pos.Repository.Menu.MenuItemRepository;
import pos.pos.Service.Interfecaes.Menu.ItemVariantService;
import pos.pos.Util.OrderingManger;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemVariantServiceImpl implements ItemVariantService {

    private final MenuItemRepository itemRepository;
    private final ItemVariantRepository variantRepository;
    private final ItemVariantMapper mapper;

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
                .orElseThrow(() -> new ItemVariantNotFound(menuId, sectionId, itemId, variantId));
        return mapper.toResponse(variant);
    }

    @Override
    @Transactional
    public ItemVariantResponse createVariant(Long menuId, Long sectionId, Long itemId, ItemVariantCreateRequest request) {
        MenuItem item = loadItemOrThrow(menuId, sectionId, itemId);

        if (request.name() != null && variantRepository.existsByItem_IdAndNameIgnoreCase(itemId, request.name())) {
            throw new AlreadyExistsException("Menu variant", request.name());
        }

        long count = variantRepository.countByItem_Id(itemId);
        int pos = OrderingManger.clamp(
                (request.sortOrder() == null) ? (int) (count + 1) : request.sortOrder(),
                1, (int) count + 1
        );

        variantRepository.shiftRightFrom(itemId, pos);

        ItemVariant entity = mapper.toEntity(request, item);
        entity.setSortOrder(pos);

        if (Boolean.TRUE.equals(entity.isDefault())) {
            variantRepository.clearDefaultForItem(itemId);
        }

        ItemVariant saved = variantRepository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public ItemVariantResponse updateVariant(Long menuId, Long sectionId, Long itemId, Long variantId, ItemVariantUpdateRequest request) {
        loadItemOrThrow(menuId, sectionId, itemId);

        ItemVariant entity = variantRepository.findByIdAndItem_Id(variantId, itemId)
                .orElseThrow(() -> new MenuItemException(menuId, sectionId, itemId));

        if (request.name() != null && !request.name().equalsIgnoreCase(entity.getName())
                && variantRepository.existsByItem_IdAndNameIgnoreCase(itemId, request.name())) {
            throw new AlreadyExistsException("Menu variant ", request.name());
        }

        boolean willBeDefault = Boolean.TRUE.equals(request.isDefault());
        if (willBeDefault) {
            variantRepository.clearDefaultForItem(itemId);
        }

        mapper.updateEntity(entity, request);

        ItemVariant saved = variantRepository.save(entity);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteVariant(Long menuId, Long sectionId, Long itemId, Long variantId) {
        loadItemOrThrow(menuId, sectionId, itemId);

        ItemVariant entity = variantRepository.findByIdAndItem_Id(variantId, itemId)
                .orElseThrow(() -> new ItemVariantNotFound(menuId, sectionId, itemId, variantId));

        int oldPos = entity.getSortOrder();
        variantRepository.delete(entity);
        variantRepository.shiftLeftAfter(itemId, oldPos);
    }


    @Override
    @Transactional
    public ItemVariantResponse moveOne(Long menuId, Long sectionId, Long itemId, Long variantId, int direction) {
        loadItemOrThrow(menuId, sectionId, itemId);

        if (direction != -1 && direction != 1) {
            throw new IllegalArgumentException("direction must be -1 (up) or +1 (down)");
        }

        ItemVariant v = variantRepository.findByIdAndItem_Id(variantId, itemId)
                .orElseThrow(() -> new ItemVariantNotFound(menuId, sectionId, itemId, variantId));

        int oldPos = v.getSortOrder();
        int targetPos = oldPos + direction;

        long count = variantRepository.countByItem_Id(itemId);
        if (targetPos < 1 || targetPos > count) {
            return mapper.toResponse(v);
        }

        ItemVariant neighbor = variantRepository.findByItem_IdAndSortOrder(itemId, targetPos)
                .orElseThrow(() -> new IllegalStateException("Neighbor not found, data corrupted"));

        int tempPos = -v.getId().intValue();
        variantRepository.updateSortOrder(itemId, v.getId(), tempPos);
        variantRepository.updateSortOrder(itemId, neighbor.getId(), oldPos);
        variantRepository.updateSortOrder(itemId, v.getId(), targetPos);

        ItemVariant reloaded = variantRepository.findByIdAndItem_Id(v.getId(), itemId)
                .orElseThrow(() -> new ItemVariantNotFound(menuId, sectionId, itemId, v.getId()));

        return mapper.toResponse(reloaded);
    }


    private MenuItem loadItemOrThrow(Long menuId, Long sectionId, Long itemId) {
        MenuItem item = itemRepository.findByIdAndSection_Id(itemId, sectionId)
                .orElseThrow(() -> new MenuSectionNotFound(menuId, sectionId));
        if (item.getSection() == null || item.getSection().getMenu() == null || !item.getSection().getMenu().getId().equals(menuId)) {
            throw new MenuItemException(menuId, sectionId, itemId);
        }
        return item;
    }
}
