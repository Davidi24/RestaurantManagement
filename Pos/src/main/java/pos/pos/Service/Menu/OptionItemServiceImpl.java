package pos.pos.Service.Menu;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.MenuMapper.OptionItemMapper;
import pos.pos.DTO.Menu.OptionDTO.OptionItemCreateRequest;
import pos.pos.DTO.Menu.OptionDTO.OptionItemResponse;
import pos.pos.DTO.Menu.OptionDTO.OptionItemUpdateRequest;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.OptionGroup;
import pos.pos.Entity.Menu.OptionItem;
import pos.pos.Entity.Menu.MenuSection;
import pos.pos.Exeption.MenuItemException;
import pos.pos.Exeption.MenuSectionNotFound;
import pos.pos.Exeption.OptionGroupNotFoundException;
import pos.pos.Exeption.OptionItemNotFoundException;
import pos.pos.Repository.Menu.MenuItemRepository;
import pos.pos.Repository.Menu.MenuSectionRepository;
import pos.pos.Repository.Order.OptionGroupRepository;
import pos.pos.Repository.Order.OptionItemRepository;
import pos.pos.Service.Interfecaes.OptionItemService;

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

        // Optional uniqueness guard per group
        // if (optionRepo.existsByGroup_IdAndNameIgnoreCase(groupId, body.name())) {
        //     throw new AlreadyExistsException("OptionItem", body.name());
        // }

        OptionItem entity = mapper.toEntity(body, group);

        if (entity.getSortOrder() == null) {
            int next = Math.toIntExact(optionRepo.countByGroup_Id(groupId));
            entity.setSortOrder(next);
        }

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

        mapper.apply(body, oi);
        return mapper.toResponse(optionRepo.save(oi));
    }

    @Override
    public void delete(Long menuId, Long sectionId, Long itemId, Long groupId, Long optionId) {
        requireGroup(menuId, sectionId, itemId, groupId);
        OptionItem oi = optionRepo.findByIdAndGroup_Id(optionId, groupId)
                .orElseThrow(() -> new OptionItemNotFoundException(menuId, sectionId, itemId, groupId, optionId));
        optionRepo.delete(oi);
    }

    /** Validates: section belongs to menu, item belongs to section, group belongs to item. Returns the group. */
    private OptionGroup requireGroup(Long menuId, Long sectionId, Long itemId, Long groupId) {
        MenuSection section = sectionRepo.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new MenuSectionNotFound(menuId, sectionId));

        MenuItem item = itemRepo.findByIdAndSection_Id(itemId, section.getId())
                .orElseThrow(() -> new MenuItemException(menuId, sectionId, itemId));

        return groupRepo.findByIdAndItem_Id(groupId, item.getId())
                .orElseThrow(() -> new OptionGroupNotFoundException(menuId, sectionId, itemId, groupId));
    }
}
