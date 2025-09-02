package pos.pos.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.OptionGroupMapper;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupCreateRequest;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupResponse;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupUpdateRequest;
import pos.pos.Entity.Menu.MenuItem;
import pos.pos.Entity.Menu.OptionGroup;
import pos.pos.Entity.Menu.MenuSection;
import pos.pos.Exeption.MenuItemException;
import pos.pos.Exeption.MenuSectionNotFound;
import pos.pos.Exeption.OptionGroupNotFoundException;
import pos.pos.Repository.MenuItemRepository;
import pos.pos.Repository.MenuSectionRepository;
import pos.pos.Repository.OptionGroupRepository;
import pos.pos.Service.Interfecaes.OptionGroupService;

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

        OptionGroup entity = mapper.toEntity(body, parent);
        if (entity.getSortOrder() == null) {
            int next = Math.toIntExact(groupRepo.countByItem_Id(parent.getId()));
            entity.setSortOrder(next);
        }
        return mapper.toResponse(groupRepo.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public OptionGroupResponse getById(Long menuId, Long sectionId, Long itemId, Long groupId) {
        // Ensure parent chain is valid first
        requireItem(menuId, sectionId, itemId);
        OptionGroup g = groupRepo.findByIdAndItem_Id(groupId, itemId)
                .orElseThrow(() -> new OptionGroupNotFoundException(menuId,sectionId,itemId,groupId));
        return mapper.toResponse(g);
    }

    @Override
    public OptionGroupResponse patch(Long menuId, Long sectionId, Long itemId, Long groupId, OptionGroupUpdateRequest body) {
        requireItem(menuId, sectionId, itemId);
        OptionGroup g = groupRepo.findByIdAndItem_Id(groupId, itemId)
                .orElseThrow(() ->  new OptionGroupNotFoundException(menuId,sectionId,itemId,groupId));

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
                .orElseThrow(() ->  new OptionGroupNotFoundException(menuId,sectionId,itemId,groupId));
        groupRepo.delete(g);
    }


    private MenuItem requireItem(Long menuId, Long sectionId, Long itemId) {
        MenuSection section = sectionRepo.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new MenuSectionNotFound(menuId, sectionId));

        return itemRepo.findByIdAndSection_Id(itemId, section.getId())
                .orElseThrow(() -> new MenuItemException(menuId, sectionId, itemId));
    }
}
