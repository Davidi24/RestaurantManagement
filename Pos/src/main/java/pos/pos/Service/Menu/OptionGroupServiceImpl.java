package pos.pos.Service.Menu;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.MenuMapper.OptionGroupMapper;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupCreateRequest;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupResponse;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupUpdateRequest;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.MenuSection;
import pos.pos.Entity.Menu.OptionGroup;
import pos.pos.Exeption.General.AlreadyExistsException;
import pos.pos.Exeption.MenuItemException;
import pos.pos.Exeption.MenuSectionNotFound;
import pos.pos.Exeption.OptionGroupNotFoundException;
import pos.pos.Repository.Menu.MenuItemRepository;
import pos.pos.Repository.Menu.MenuSectionRepository;
import pos.pos.Repository.Order.OptionGroupRepository;
import pos.pos.Service.Interfecaes.Menu.OptionGroupService;
import pos.pos.Util.OrderingManger;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OptionGroupServiceImpl implements OptionGroupService {

    private final OptionGroupRepository groupRepo;
    private final MenuItemRepository itemRepo;
    private final MenuSectionRepository sectionRepo;
    private final OptionGroupMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<OptionGroupResponse> listByItem(Long menuId, Long sectionId, Long itemId) {
        MenuItem parent = requireItem(menuId, sectionId, itemId);
        return groupRepo.findByItem_IdOrderBySortOrderAscIdAsc(parent.getId())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public OptionGroupResponse create(Long menuId, Long sectionId, Long itemId, OptionGroupCreateRequest body) {
        MenuItem parent = requireItem(menuId, sectionId, itemId);

        if (body.name() != null && groupRepo.existsByItem_IdAndNameIgnoreCase(parent.getId(), body.name())) {
            throw new AlreadyExistsException("Option group", body.name());
        }

        long count = groupRepo.countByItem_Id(parent.getId());
        int pos = OrderingManger.clamp(
                (body.sortOrder() == null) ? (int) (count + 1) : body.sortOrder(),
                1, (int) count + 1
        );

        groupRepo.shiftRightFrom(parent.getId(), pos);

        OptionGroup entity = mapper.toEntity(body, parent);
        entity.setSortOrder(pos);

        return mapper.toResponse(groupRepo.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public OptionGroupResponse getById(Long menuId, Long sectionId, Long itemId, Long groupId) {
        requireItem(menuId, sectionId, itemId);
        OptionGroup g = groupRepo.findByIdAndItem_Id(groupId, itemId)
                .orElseThrow(() -> new OptionGroupNotFoundException(menuId, sectionId, itemId, groupId));
        return mapper.toResponse(g);
    }

    @Override
    public OptionGroupResponse patch(Long menuId, Long sectionId, Long itemId, Long groupId, OptionGroupUpdateRequest body) {
        requireItem(menuId, sectionId, itemId);
        OptionGroup g = groupRepo.findByIdAndItem_Id(groupId, itemId)
                .orElseThrow(() -> new OptionGroupNotFoundException(menuId, sectionId, itemId, groupId));

        if (body.name() != null && !body.name().equalsIgnoreCase(g.getName())
                && groupRepo.existsByItem_IdAndNameIgnoreCase(itemId, body.name())) {
            throw new AlreadyExistsException("Option group", body.name());
        }

        if (body.required() != null && body.required()) {
            Integer min = body.minSelections() != null ? body.minSelections() : g.getMinSelections();
            Integer max = body.maxSelections() != null ? body.maxSelections() : g.getMaxSelections();
            if (min != null && max != null && min > max) {
                throw new IllegalArgumentException("minSelections cannot be greater than maxSelections.");
            }
        }

        mapper.apply(body, g);
        return mapper.toResponse(groupRepo.save(g));
    }

    @Override
    public void delete(Long menuId, Long sectionId, Long itemId, Long groupId) {
        requireItem(menuId, sectionId, itemId);
        OptionGroup g = groupRepo.findByIdAndItem_Id(groupId, itemId)
                .orElseThrow(() -> new OptionGroupNotFoundException(menuId, sectionId, itemId, groupId));
        int oldPos = g.getSortOrder();
        groupRepo.delete(g);
        groupRepo.shiftLeftAfter(itemId, oldPos);
    }

    @Override
    public OptionGroupResponse moveOne(Long menuId, Long sectionId, Long itemId, Long groupId, int direction) {
        requireItem(menuId, sectionId, itemId);

        if (direction != -1 && direction != 1) {
            throw new IllegalArgumentException("direction must be -1 (up) or +1 (down)");
        }

        OptionGroup g = groupRepo.findByIdAndItem_Id(groupId, itemId)
                .orElseThrow(() -> new OptionGroupNotFoundException(menuId, sectionId, itemId, groupId));

        int oldPos = g.getSortOrder();
        int targetPos = oldPos + direction;

        long count = groupRepo.countByItem_Id(itemId);
        if (targetPos < 1 || targetPos > count) {
            return mapper.toResponse(g);
        }

        OptionGroup neighbor = groupRepo.findByItem_IdAndSortOrder(itemId, targetPos)
                .orElseThrow(() -> new IllegalStateException("Neighbor not found, data corrupted"));

        int tempPos = -g.getId().intValue();
        groupRepo.updateSortOrder(itemId, g.getId(), tempPos);
        groupRepo.updateSortOrder(itemId, neighbor.getId(), oldPos);
        groupRepo.updateSortOrder(itemId, g.getId(), targetPos);

        OptionGroup reloaded = groupRepo.findByIdAndItem_Id(g.getId(), itemId)
                .orElseThrow(() -> new OptionGroupNotFoundException(menuId, sectionId, itemId, g.getId()));

        return mapper.toResponse(reloaded);
    }

    private MenuItem requireItem(Long menuId, Long sectionId, Long itemId) {
        MenuSection section = sectionRepo.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new MenuSectionNotFound(menuId, sectionId));

        return itemRepo.findByIdAndSection_Id(itemId, section.getId())
                .orElseThrow(() -> new MenuItemException(menuId, sectionId, itemId));
    }
}
