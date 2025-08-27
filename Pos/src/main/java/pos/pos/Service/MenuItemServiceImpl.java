package pos.pos.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.MenuItemMapper;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemCreateRequest;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemResponse;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemUpdateRequest;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.MenuSection;
import pos.pos.Exeption.AlreadyExistsException;
import pos.pos.Exeption.MenuItemExeption;
import pos.pos.Exeption.MenuSectionNotFound;
import pos.pos.Repository.MenuItemRepository;
import pos.pos.Repository.MenuSectionRepository;
import pos.pos.Service.Interfecaes.MenuItemService;

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
                .stream().map(mapper::toMenuItemResponse).toList();
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
                .orElseThrow(() -> new MenuItemExeption(menuId,sectionId, itemId));

        if (!item.getName().equalsIgnoreCase(req.name())
                && itemRepository.existsBySection_IdAndNameIgnoreCase(sectionId, req.name())) {
            throw new AlreadyExistsException("MenuItem", req.name());
        }

        Integer requestedPos = req.sortOrder();
        if (requestedPos != null && !requestedPos.equals(item.getSortOrder())) {
            long count = itemRepository.countBySection_Id(sectionId);
            int newPos = OrderingManger.clamp(requestedPos, 1, (int) count);
            shiftForMove(sectionId, item.getSortOrder(), newPos);
            item.setSortOrder(newPos);
        }

        mapper.apply(req, item);
        item = itemRepository.save(item);
        return mapper.toMenuItemResponse(item);
    }

    @Override
    @Transactional
    public void deleteItem(Long menuId, Long sectionId, Long itemId) {
        requireSection(menuId, sectionId);

        MenuItem item = itemRepository.findByIdAndSection_Id(itemId, sectionId)
                .orElseThrow(() -> new MenuItemExeption(menuId,sectionId, itemId));

        int oldPos = item.getSortOrder();
        itemRepository.delete(item);
        itemRepository.shiftLeftAfter(sectionId, oldPos);
    }

    @Override
    @Transactional
    public MenuItemResponse moveItem(Long menuId, Long sectionId, Long itemId, int newSortOrder) {
        requireSection(menuId, sectionId);

        MenuItem item = itemRepository.findByIdAndSection_Id(itemId, sectionId)
                .orElseThrow(() -> new MenuItemExeption(menuId,sectionId, itemId));

        long count = itemRepository.countBySection_Id(sectionId);
        int newPos = OrderingManger.clamp(newSortOrder, 1, (int) count);
        int oldPos = item.getSortOrder();
        if (newPos == oldPos) return mapper.toMenuItemResponse(item);

        shiftForMove(sectionId, oldPos, newPos);
        item.setSortOrder(newPos);
        item = itemRepository.save(item);

        return mapper.toMenuItemResponse(item);
    }

    private MenuSection requireSection(Long menuId, Long sectionId) {
        return sectionRepository.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new MenuSectionNotFound(menuId,sectionId));
    }

    private void shiftForMove(Long sectionId, int oldPos, int newPos) {
        if (newPos < oldPos) {
            itemRepository.shiftRightRange(sectionId, newPos, oldPos);
        } else {
            itemRepository.shiftLeftRange(sectionId, oldPos, newPos);
        }
    }
}
