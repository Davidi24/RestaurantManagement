package pos.pos.Service.Menu;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.MenuMapper.OptionItemMapper;
import pos.pos.DTO.Menu.OptionDTO.OptionItemCreateRequest;
import pos.pos.DTO.Menu.OptionDTO.OptionItemResponse;
import pos.pos.DTO.Menu.OptionDTO.OptionItemUpdateRequest;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.MenuSection;
import pos.pos.Entity.Menu.OptionGroup;
import pos.pos.Entity.Menu.OptionItem;
import pos.pos.Exeption.General.AlreadyExistsException;
import pos.pos.Exeption.MenuItemException;
import pos.pos.Exeption.MenuSectionNotFound;
import pos.pos.Exeption.OptionGroupNotFoundException;
import pos.pos.Exeption.OptionItemNotFoundException;
import pos.pos.Repository.Menu.MenuItemRepository;
import pos.pos.Repository.Menu.MenuSectionRepository;
import pos.pos.Repository.Order.OptionGroupRepository;
import pos.pos.Repository.Order.OptionItemRepository;
import pos.pos.Service.Interfecaes.Menu.OptionItemService;
import pos.pos.Util.OrderingManger;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OptionItemServiceImpl implements OptionItemService {

    private final OptionItemRepository optionRepo;
    private final OptionGroupRepository groupRepo;
    private final MenuItemRepository itemRepo;
    private final MenuSectionRepository sectionRepo;
    private final OptionItemMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<OptionItemResponse> list(Long menuId, Long sectionId, Long itemId, Long groupId) {
        requireGroup(menuId, sectionId, itemId, groupId);
        return optionRepo.findByGroup_IdOrderBySortOrderAscIdAsc(groupId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public OptionItemResponse create(Long menuId, Long sectionId, Long itemId, Long groupId, OptionItemCreateRequest body) {
        OptionGroup group = requireGroup(menuId, sectionId, itemId, groupId);

        if (body.name() != null && optionRepo.existsByGroup_IdAndNameIgnoreCase(groupId, body.name())) {
            throw new AlreadyExistsException("Option item", body.name());
        }

        long count = optionRepo.countByGroup_Id(groupId);
        int pos = OrderingManger.clamp(
                (body.sortOrder() == null) ? (int) (count + 1) : body.sortOrder(),
                1, (int) count + 1
        );

        optionRepo.shiftRightFrom(groupId, pos);

        OptionItem entity = mapper.toEntity(body, group);
        entity.setSortOrder(pos);

        return mapper.toResponse(optionRepo.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public OptionItemResponse get(Long menuId, Long sectionId, Long itemId, Long groupId, Long optionId) {
        requireGroup(menuId, sectionId, itemId, groupId);
        OptionItem oi = optionRepo.findByIdAndGroup_Id(optionId, groupId)
                .orElseThrow(() -> new OptionItemNotFoundException(menuId, sectionId, itemId, groupId, optionId));
        return mapper.toResponse(oi);
    }

    @Override
    public OptionItemResponse patch(Long menuId, Long sectionId, Long itemId, Long groupId, Long optionId, OptionItemUpdateRequest body) {
        requireGroup(menuId, sectionId, itemId, groupId);
        OptionItem oi = optionRepo.findByIdAndGroup_Id(optionId, groupId)
                .orElseThrow(() -> new OptionItemNotFoundException(menuId, sectionId, itemId, groupId, optionId));

        if (body.name() != null && !body.name().equalsIgnoreCase(oi.getName())
                && optionRepo.existsByGroup_IdAndNameIgnoreCase(groupId, body.name())) {
            throw new AlreadyExistsException("Option item", body.name());
        }

        mapper.apply(body, oi);
        return mapper.toResponse(optionRepo.save(oi));
    }

    @Override
    public void delete(Long menuId, Long sectionId, Long itemId, Long groupId, Long optionId) {
        requireGroup(menuId, sectionId, itemId, groupId);
        OptionItem oi = optionRepo.findByIdAndGroup_Id(optionId, groupId)
                .orElseThrow(() -> new OptionItemNotFoundException(menuId, sectionId, itemId, groupId, optionId));
        int oldPos = oi.getSortOrder();
        optionRepo.delete(oi);
        optionRepo.shiftLeftAfter(groupId, oldPos);
    }

    @Override
    public OptionItemResponse moveOne(Long menuId, Long sectionId, Long itemId, Long groupId, Long optionId, int direction) {
        requireGroup(menuId, sectionId, itemId, groupId);

        if (direction != -1 && direction != 1) {
            throw new IllegalArgumentException("direction must be -1 (up) or +1 (down)");
        }

        OptionItem oi = optionRepo.findByIdAndGroup_Id(optionId, groupId)
                .orElseThrow(() -> new OptionItemNotFoundException(menuId, sectionId, itemId, groupId, optionId));

        int oldPos = oi.getSortOrder();
        int targetPos = oldPos + direction;

        long count = optionRepo.countByGroup_Id(groupId);
        if (targetPos < 1 || targetPos > count) {
            return mapper.toResponse(oi);
        }

        OptionItem neighbor = optionRepo.findByGroup_IdAndSortOrder(groupId, targetPos)
                .orElseThrow(() -> new IllegalStateException("Neighbor not found, data corrupted"));

        int tempPos = -oi.getId().intValue();
        optionRepo.updateSortOrder(groupId, oi.getId(), tempPos);
        optionRepo.updateSortOrder(groupId, neighbor.getId(), oldPos);
        optionRepo.updateSortOrder(groupId, oi.getId(), targetPos);

        OptionItem reloaded = optionRepo.findByIdAndGroup_Id(oi.getId(), groupId)
                .orElseThrow(() -> new OptionItemNotFoundException(menuId, sectionId, itemId, groupId, oi.getId()));

        return mapper.toResponse(reloaded);
    }

    private OptionGroup requireGroup(Long menuId, Long sectionId, Long itemId, Long groupId) {
        MenuSection section = sectionRepo.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new MenuSectionNotFound(menuId, sectionId));

        MenuItem item = itemRepo.findByIdAndSection_Id(itemId, section.getId())
                .orElseThrow(() -> new MenuItemException(menuId, sectionId, itemId));

        return groupRepo.findByIdAndItem_Id(groupId, item.getId())
                .orElseThrow(() -> new OptionGroupNotFoundException(menuId, sectionId, itemId, groupId));
    }
}
