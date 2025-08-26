package pos.pos.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.MenuSectionMapper;
import pos.pos.DTO.Menu.MenuSectionCreateRequest;
import pos.pos.DTO.Menu.MenuSectionResponse;
import pos.pos.DTO.Menu.MenuSectionUpdateRequest;
import pos.pos.Entity.Menu.Menu;
import pos.pos.Entity.Menu.MenuSection;
import pos.pos.Exeption.AlreadyExistsException;
import pos.pos.Exeption.MenuNotFoundException;
import pos.pos.Exeption.MenuSectionNotFound;
import pos.pos.Repository.MenuRepository;
import pos.pos.Repository.MenuSectionRepository;
import pos.pos.Service.Interfecaes.MenuSectionService;

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
        var list = sectionRepository.findByMenu_IdOrderByOrderKeyAsc(menuId);
        int[] i = {1};
        return list.stream()
                .map(s -> mapper.toMenuSectionResponse(s, i[0]++))
                .toList();
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

        BigDecimal key = OrderingManger.computeInsertKeyDecimal(
                req.sortOrder(),
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
                .orElseThrow(() -> new MenuSectionNotFound(menuId,sectionId));
        if (!section.getName().equalsIgnoreCase(req.name())
                && sectionRepository.existsByMenu_IdAndNameIgnoreCase(menuId, req.name())) {
            throw new AlreadyExistsException("Menu Section", req.name());
        }
        mapper.apply(req, section);
        section = sectionRepository.save(section);
        return toResponse(section);
    }

    @Override
    @Transactional
    public void deleteSection(Long menuId, Long sectionId) {
        MenuSection section = sectionRepository.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new MenuSectionNotFound(menuId,sectionId));
        sectionRepository.delete(section);
    }

    @Override
    @Transactional
    public MenuSectionResponse moveSection(Long menuId, Long sectionId, int newSortOrder) {
        MenuSection section = sectionRepository.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new MenuSectionNotFound(menuId,sectionId));

        long total = Math.max(0, sectionRepository.countByMenu_Id(menuId) - 1);

        BigDecimal key = OrderingManger.computeMoveKeyDecimal(
                newSortOrder,
                total,
                () -> nthExcluding(menuId, sectionId, 0).orElseThrow().getOrderKey(),
                () -> sectionRepository.findFirstByMenu_IdOrderByOrderKeyDesc(menuId).orElseThrow().getOrderKey(),
                idx -> nthExcluding(menuId, sectionId, idx).orElseThrow().getOrderKey(),
                () -> sectionRepository.rebalance(menuId),
                section.getOrderKey()
        );

        section.setOrderKey(key);
        section = sectionRepository.save(section);
        return toResponse(section);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuSectionResponse getSection(Long menuId, Long sectionId) {
        MenuSection section = sectionRepository.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new MenuSectionNotFound(menuId,sectionId));
        return toResponse(section);
    }

    private Optional<MenuSection> nth(Long menuId, int index) {
        if (index < 0) return Optional.empty();
        Pageable p = PageRequest.of(index, 1);
        List<MenuSection> list = sectionRepository.findByMenu_IdOrderByOrderKeyAsc(menuId, p);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private Optional<MenuSection> nthExcluding(Long menuId, Long excludeId, int index) {
        if (index < 0) return Optional.empty();
        Pageable p = PageRequest.of(index, 1);
        List<MenuSection> list = sectionRepository.findByMenu_IdAndIdNotOrderByOrderKeyAsc(menuId, excludeId, p);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    private MenuSectionResponse toResponse(MenuSection s) {
        long zeroBased = sectionRepository.countBefore(s.getMenu().getId(), s.getOrderKey());
        int position1Based = Math.toIntExact(zeroBased + 1);
        return mapper.toMenuSectionResponse(s, position1Based);
    }
}

