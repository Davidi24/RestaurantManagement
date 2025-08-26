package pos.pos.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.MenuSectionMapper;
import pos.pos.DTO.MenuSectionCreateRequest;
import pos.pos.DTO.MenuSectionResponse;
import pos.pos.DTO.MenuSectionUpdateRequest;
import pos.pos.Entity.Menu.Menu;
import pos.pos.Entity.Menu.MenuSection;
import pos.pos.Exeption.MenuNotFoundException;
import pos.pos.Repository.MenuRepository;
import pos.pos.Repository.MenuSectionRepository;
import pos.pos.Service.Interfecaes.MenuSectionService;


import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuSectionServiceImpl implements MenuSectionService {

    private final MenuRepository menuRepository;
    private final MenuSectionRepository sectionRepository;
    private final MenuSectionMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public List<MenuSectionResponse> listSections(Long menuId) {
        return sectionRepository.findByMenu_IdOrderBySortOrderAscIdAsc(menuId)
                .stream().map(mapper::toMenuSectionResponse).toList();
    }

    @Override
    @Transactional
    public MenuSectionResponse createSection(Long menuId, MenuSectionCreateRequest req) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuNotFoundException(menuId));
        if (sectionRepository.existsByMenu_IdAndNameIgnoreCase(menuId, req.name())) {
            throw new IllegalArgumentException("Section name already exists in this menu");
        }

        MenuSection section = mapper.toMenuSection(req);
        section.setMenu(menu);

        if (section.getSortOrder() == null) {
            int max = sectionRepository.findByMenu_IdOrderBySortOrderAscIdAsc(menuId)
                    .stream().mapToInt(s -> s.getSortOrder() == null ? 0 : s.getSortOrder()).max().orElse(-1);
            section.setSortOrder(max + 1);
        }

        return mapper.toMenuSectionResponse(sectionRepository.save(section));
    }

    @Override
    @Transactional
    public MenuSectionResponse updateSection(Long menuId, Long sectionId, MenuSectionUpdateRequest req) {
        MenuSection section = sectionRepository.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new RuntimeException("Section not found: " + sectionId));

        if (!section.getName().equalsIgnoreCase(req.name())
                && sectionRepository.existsByMenu_IdAndNameIgnoreCase(menuId, req.name())) {
            throw new IllegalArgumentException("Section name already exists in this menu");
        }

        mapper.apply(req, section);
        return mapper.toMenuSectionResponse(section);
    }

    @Override
    @Transactional
    public void deleteSection(Long menuId, Long sectionId) {
        MenuSection section = sectionRepository.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new RuntimeException("Section not found: " + sectionId));
        sectionRepository.delete(section);
    }

    @Override
    @Transactional
    public MenuSectionResponse moveSection(Long menuId, Long sectionId, int newSortOrder) {
        MenuSection section = sectionRepository.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new RuntimeException("Section not found: " + sectionId));
        section.setSortOrder(newSortOrder);
        return mapper.toMenuSectionResponse(section);
    }

    @Override
    @Transactional(readOnly = true)
    public MenuSectionResponse getSection(Long menuId, Long sectionId) {
        MenuSection section = sectionRepository.findByIdAndMenu_Id(sectionId, menuId)
                .orElseThrow(() -> new RuntimeException("Section not found: " + sectionId));
        return mapper.toMenuSectionResponse(section);
    }
}
