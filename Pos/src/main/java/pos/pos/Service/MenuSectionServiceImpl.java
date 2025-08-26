package pos.pos.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.MenuSectionMapper;
import pos.pos.DTO.MenuSectionCreateRequest;
import pos.pos.DTO.MenuSectionResponse;
import pos.pos.DTO.MenuSectionUpdateRequest;
import pos.pos.Entity.Menu.Menu;
import pos.pos.Entity.Menu.MenuSection;
import pos.pos.Exeption.AlreadyExistsException;
import pos.pos.Exeption.MenuNotFoundException;
import pos.pos.Exeption.MenuSectionNotFound;
import pos.pos.Repository.MenuRepository;
import pos.pos.Repository.MenuSectionRepository;
import pos.pos.Service.Interfecaes.MenuSectionService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MenuSectionServiceImpl implements MenuSectionService {

    private final MenuRepository menuRepository;
    private final MenuSectionRepository sectionRepository;
    private final MenuSectionMapper mapper;

    private static final BigDecimal STEP = new BigDecimal("1000");
    private static final BigDecimal ONE = BigDecimal.ONE;
    private static final int SCALE = 6;
    private static final BigDecimal EPS = new BigDecimal("0.000001");


    @Override
    @Transactional(readOnly = true)
    public List<MenuSectionResponse> listSections(Long menuId) {
        var list = sectionRepository.findByMenu_IdOrderByOrderKeyAsc(menuId);
        int[] i = {1}; // 1-based
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
        Integer target = req.sortOrder();
        BigDecimal key;

        if (count == 0) {
            key = STEP;
        } else {
            int pos = (target == null)
                    ? (int) count
                    : Math.max(0, Math.min(target - 1, (int) count));

            if (pos == 0) {
                BigDecimal first = sectionRepository.findFirstByMenu_IdOrderByOrderKeyAsc(menuId).orElseThrow().getOrderKey();
                key = first.subtract(ONE);
            } else if (pos == count) {
                BigDecimal last = sectionRepository.findFirstByMenu_IdOrderByOrderKeyDesc(menuId).orElseThrow().getOrderKey();
                key = last.add(STEP);
            } else {
                BigDecimal left = nth(menuId, pos - 1).orElseThrow().getOrderKey();
                BigDecimal right = nth(menuId, pos).orElseThrow().getOrderKey();
                key = mid(left, right);
                if (right.subtract(left).compareTo(EPS) <= 0) {
                    sectionRepository.rebalance(menuId);
                    left = nth(menuId, pos - 1).orElseThrow().getOrderKey();
                    right = nth(menuId, pos).orElseThrow().getOrderKey();
                    key = mid(left, right);
                }
            }
        }

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
                .orElseThrow(() -> new MenuSectionNotFound(sectionId));
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
                .orElseThrow(() -> new MenuSectionNotFound(sectionId));
        sectionRepository.delete(section);
    }

    @Override
    @Transactional
    public MenuSectionResponse moveSection(Long menuId, Long sectionId, int newSortOrder) {
        MenuSection section = sectionRepository.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new MenuSectionNotFound(sectionId));

        long total = Math.max(0, sectionRepository.countByMenu_Id(menuId) - 1);
        int pos = Math.max(0, Math.min(newSortOrder - 1, (int) total));

        BigDecimal key;

        if (total == 0) {
            key = STEP;
        } else if (pos == 0) {
            BigDecimal first = nthExcluding(menuId, sectionId, 0).orElseThrow().getOrderKey();
            key = first.subtract(ONE);
        } else if (pos == total) {
            BigDecimal last = sectionRepository.findFirstByMenu_IdOrderByOrderKeyDesc(menuId).orElseThrow().getOrderKey();
            if (section.getOrderKey() != null && last.compareTo(section.getOrderKey()) == 0) {
                key = last; // already last
            } else {
                key = last.add(STEP);
            }
        } else {
            BigDecimal left = nthExcluding(menuId, sectionId, pos - 1).orElseThrow().getOrderKey();
            BigDecimal right = nthExcluding(menuId, sectionId, pos).orElseThrow().getOrderKey();
            key = mid(left, right);
            if (right.subtract(left).compareTo(EPS) <= 0) {
                sectionRepository.rebalance(menuId);
                left = nthExcluding(menuId, sectionId, pos - 1).orElseThrow().getOrderKey();
                right = nthExcluding(menuId, sectionId, pos).orElseThrow().getOrderKey();
                key = mid(left, right);
            }
        }

        section.setOrderKey(key);
        section = sectionRepository.save(section);
        return toResponse(section); // returns 1-based position
    }

    @Override
    @Transactional(readOnly = true)
    public MenuSectionResponse getSection(Long menuId, Long sectionId) {
        MenuSection section = sectionRepository.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new MenuSectionNotFound(sectionId));
        return toResponse(section);
    }

    private BigDecimal mid(BigDecimal a, BigDecimal b) {
        return a.add(b).divide(new BigDecimal("2"), SCALE, RoundingMode.HALF_UP);
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
