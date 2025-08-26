package pos.pos.Service.Interfecaes;

import pos.pos.DTO.MenuSectionCreateRequest;
import pos.pos.DTO.MenuSectionResponse;
import pos.pos.DTO.MenuSectionUpdateRequest;

import java.util.List;

public interface MenuSectionService {

    List<MenuSectionResponse> listSections(Long menuId);

    MenuSectionResponse createSection(Long menuId, MenuSectionCreateRequest req);

    MenuSectionResponse updateSection(Long menuId, Long sectionId, MenuSectionUpdateRequest req);

    void deleteSection(Long menuId, Long sectionId);

    MenuSectionResponse moveSection(Long menuId, Long sectionId, int newSortOrder);

    MenuSectionResponse getSection(Long menuId, Long sectionId);
}
