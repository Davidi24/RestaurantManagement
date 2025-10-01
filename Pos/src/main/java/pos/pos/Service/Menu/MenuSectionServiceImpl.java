package pos.pos.Service.Menu;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.MenuMapper.MenuSectionMapper;
import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionCreateRequest;
import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionResponse;
import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionUpdateRequest;
import pos.pos.Entity.Menu.Menu;
import pos.pos.Entity.Menu.MenuSection;
import pos.pos.Exeption.General.AlreadyExistsException;
import pos.pos.Exeption.MenuNotFoundException;
import pos.pos.Exeption.MenuSectionNotFound;
import pos.pos.Repository.Menu.MenuRepository;
import pos.pos.Repository.Menu.MenuSectionRepository;
import pos.pos.Service.Interfecaes.Menu.MenuSectionService;
import pos.pos.Util.OrderingManger;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuSectionServiceImpl implements MenuSectionService {

    private final MenuRepository menuRepository;
    private final MenuSectionRepository sectionRepository;
    private final MenuSectionMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<MenuSectionResponse> listSections(Long menuId) {
        var sections = sectionRepository.findByMenu_IdOrderByOrderKeyAsc(menuId);
        return mapper.toMenuSectionResponse(sections);
    }

    @Override
    @Transactional
    public MenuSectionResponse createSection(Long menuId, MenuSectionCreateRequest req) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuNotFoundException(menuId));
        if (sectionRepository.existsByMenu_IdAndNameIgnoreCase(menuId, req.name())) {
            throw new AlreadyExistsException("MenuSection", req.name());
        }
        long count = sectionRepository.countByMenu_Id(menuId);
        int desired = req.sortOrder() == null ? (int) (count + 1) : req.sortOrder();
        int target = clampPosition(desired, count);

        BigDecimal key = OrderingManger.computeInsertKeyDecimal(
                target,
                count,
                () -> sectionRepository.findFirstByMenu_IdOrderByOrderKeyAsc(menuId).orElseThrow().getOrderKey(),
                () -> sectionRepository.findFirstByMenu_IdOrderByOrderKeyDesc(menuId).orElseThrow().getOrderKey(),
                idx -> nth(menuId, idx).orElseThrow().getOrderKey(),
                () -> sectionRepository.rebalance(menuId)
        );

        MenuSection section = mapper.toMenuSection(req);
        section.setMenu(menu);
        section.setOrderKey(key);
        section.setSortOrder(null);
        section = sectionRepository.save(section);

        return toResponse(section);
    }

    @Override
    @Transactional
    public MenuSectionResponse updateSection(Long menuId, Long sectionId, MenuSectionUpdateRequest req) {
        MenuSection section = sectionRepository.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new MenuSectionNotFound(menuId, sectionId));

        if (req.name() != null) {
            String current = section.getName();
            String incoming = req.name();
            boolean changed = current == null || !current.equalsIgnoreCase(incoming);
            if (changed && sectionRepository.existsByMenu_IdAndNameIgnoreCase(menuId, incoming)) {
                throw new AlreadyExistsException("MenuSection", incoming);
            }
        }

        mapper.update(req, section);
        return toResponse(section);
    }

    @Override
    @Transactional
    public void deleteSection(Long menuId, Long sectionId) {
        MenuSection section = sectionRepository.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new MenuSectionNotFound(menuId, sectionId));
        sectionRepository.delete(section);
    }

    @Override
    @Transactional
    public MenuSectionResponse moveSection(Long menuId, Long sectionId, int newSortOrder) {
        MenuSection section = sectionRepository.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new MenuSectionNotFound(menuId, sectionId));

        long totalOthers = Math.max(0, sectionRepository.countByMenu_Id(menuId) - 1);

        BigDecimal key = OrderingManger.computeMoveKeyDecimal(
                newSortOrder,
                totalOthers,
                () -> nthExcluding(menuId, sectionId, 0).orElseThrow().getOrderKey(),
                () -> sectionRepository.findFirstByMenu_IdOrderByOrderKeyDesc(menuId).orElseThrow().getOrderKey(),
                idx -> nthExcluding(menuId, sectionId, idx).orElseThrow().getOrderKey(),
                () -> sectionRepository.rebalance(menuId),
                section.getOrderKey()
        );

        section.setOrderKey(key);
        return toResponse(section);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuSectionResponse getSection(Long menuId, Long sectionId) {
        MenuSection section = sectionRepository.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new MenuSectionNotFound(menuId, sectionId));
        return toResponse(section);
    }

    private Optional<MenuSection> nth(Long menuId, int index) {
        if (index < 0) return Optional.empty();
        Pageable p = PageRequest.of(index, 1);
        List<MenuSection> list = sectionRepository.findByMenu_IdOrderByOrderKeyAsc(menuId, p);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.getFirst());
    }

    private Optional<MenuSection> nthExcluding(Long menuId, Long excludeId, int index) {
        if (index < 0) return Optional.empty();
        Pageable p = PageRequest.of(index, 1);
        List<MenuSection> list = sectionRepository.findByMenu_IdAndIdNotOrderByOrderKeyAsc(menuId, excludeId, p);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.getFirst());
    }

    private MenuSectionResponse toResponse(MenuSection s) {
        long zeroBased = sectionRepository.countBefore(s.getMenu().getId(), s.getOrderKey());
        int position1Based = Math.toIntExact(zeroBased + 1);
        return mapper.toMenuSectionResponse(s, position1Based);
    }

    // This function is used to find the position that the new section will be.
    // For example if we get count <= 0 it means no section is find in the Database
    // so put the new section in the first place. If desired is a number like -10 for
    // example then put it in the first place. And last we check if desired is bigger than
    // count + 1 which means if user puts 1000 but there are only 4 section then put it in
    // the 5 place. If not then put it when the user desires.
    private static int clampPosition(int desired, long count) {
        if (count <= 0) return 1;
        if (desired < 1) return 1;
        if (desired > count + 1) return Math.toIntExact(count + 1);
        return desired;
    }


}
