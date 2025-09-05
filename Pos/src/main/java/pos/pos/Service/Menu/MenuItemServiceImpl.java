package pos.pos.Service.Menu;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.MenuMapper.MenuItemMapper;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemCreateRequest;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemResponse;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemUpdateRequest;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.MenuSection;
import pos.pos.Exeption.AlreadyExistsException;
import pos.pos.Exeption.MenuItemException;
import pos.pos.Exeption.MenuSectionNotFound;
import pos.pos.Repository.Menu.MenuItemRepository;
import pos.pos.Repository.Menu.MenuSectionRepository;
import pos.pos.Service.Interfecaes.MenuItemService;
import pos.pos.Util.OrderingManger;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuSectionRepository sectionRepository;
    private final MenuItemRepository itemRepository;
    private final MenuItemMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<MenuItemResponse> listItems(Long menuId, Long sectionId) {
        requireSection(menuId, sectionId);
        return itemRepository.findBySection_IdOrderBySortOrderAscIdAsc(sectionId)
                .stream()
                .map(mapper::toMenuItemResponse)
                .toList();
    }

    @Override
    @Transactional
    public MenuItemResponse createItem(Long menuId, Long sectionId, MenuItemCreateRequest req) {
        MenuSection section = requireSection(menuId, sectionId);
        if (itemRepository.existsBySection_IdAndNameIgnoreCase(sectionId, req.name())) {
            throw new AlreadyExistsException("MenuItem", req.name());
        }
        long count = itemRepository.countBySection_Id(sectionId);
        int pos = OrderingManger.clamp(
                (req.sortOrder() == null) ? (int) (count + 1) : req.sortOrder(),
                1, (int) count + 1
        );
        itemRepository.shiftRightFrom(sectionId, pos);
        MenuItem item = mapper.toMenuItem(req);
        item.setSection(section);
        item.setSortOrder(pos);
        item = itemRepository.save(item);
        return mapper.toMenuItemResponse(item);
    }

    @Override
    @Transactional
    public MenuItemResponse updateItem(Long menuId, Long sectionId, Long itemId, MenuItemUpdateRequest req) {
        requireSection(menuId, sectionId);

        MenuItem item = itemRepository.findByIdAndSection_Id(itemId, sectionId)
                .orElseThrow(() -> new MenuItemException(menuId, sectionId, itemId));

        if (req.name() != null
                && !item.getName().equalsIgnoreCase(req.name())
                && itemRepository.existsBySection_IdAndNameIgnoreCase(sectionId, req.name())) {
            throw new AlreadyExistsException("MenuItem", req.name());
        }

        final int currentPos = item.getSortOrder();

        mapper.apply(req, item);
        item.setSortOrder(currentPos);

        item = itemRepository.save(item);

        MenuItem finalItem = item;
        MenuItem reloaded = itemRepository.findByIdAndSection_Id(item.getId(), sectionId)
                .orElseThrow(() -> new MenuItemException(menuId, sectionId, finalItem.getId()));
        return mapper.toMenuItemResponse(reloaded);
    }

    @Override
    @Transactional
    public void deleteItem(Long menuId, Long sectionId, Long itemId) {
        requireSection(menuId, sectionId);
        MenuItem item = itemRepository.findByIdAndSection_Id(itemId, sectionId)
                .orElseThrow(() -> new MenuItemException(menuId, sectionId, itemId));
        int oldPos = item.getSortOrder();
        itemRepository.delete(item);
        itemRepository.shiftLeftAfter(sectionId, oldPos);
    }

    @Override
    @Transactional
    public MenuItemResponse moveOne(Long menuId, Long sectionId, Long itemId, int direction) {
        requireSection(menuId, sectionId);
        if (direction != -1 && direction != 1) {
            throw new IllegalArgumentException("direction must be -1 (up) or +1 (down)");
        }
        MenuItem item = itemRepository.findByIdAndSection_Id(itemId, sectionId)
                .orElseThrow(() -> new MenuItemException(menuId, sectionId, itemId));
        int oldPos = item.getSortOrder();
        int targetPos = oldPos + direction;
        long count = itemRepository.countBySection_Id(sectionId);
        if (targetPos < 1 || targetPos > count) {
            return mapper.toMenuItemResponse(item);
        }
        MenuItem neighbor = itemRepository.findBySection_IdAndSortOrder(sectionId, targetPos)
                .orElseThrow(() -> new IllegalStateException("Neighbor not found, data corrupted"));
        int tempPos = -item.getId().intValue();
        itemRepository.updateSortOrder(sectionId, item.getId(), tempPos);
        itemRepository.updateSortOrder(sectionId, neighbor.getId(), oldPos);
        itemRepository.updateSortOrder(sectionId, item.getId(), targetPos);
        MenuItem reloaded = itemRepository.findByIdAndSection_Id(item.getId(), sectionId)
                .orElseThrow(() -> new MenuItemException(menuId, sectionId, item.getId()));
        return mapper.toMenuItemResponse(reloaded);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuItemResponse getItems(Long menuId, Long sectionId, Long itemId) {
        requireSection(menuId, sectionId);
        MenuItem item = itemRepository.findByIdAndSection_Id(itemId, sectionId)
                .orElseThrow(() -> new MenuItemException(menuId, sectionId, itemId));
        return mapper.toMenuItemResponse(item);
    }


    private MenuSection requireSection(Long menuId, Long sectionId) {
        return sectionRepository.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new MenuSectionNotFound(menuId, sectionId));
    }
}
