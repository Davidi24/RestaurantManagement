package pos.pos.Controller.Menu;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pos.pos.Config.ApiPaths;
import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionCreateRequest;
import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionResponse;
import pos.pos.DTO.Menu.MenuSectionDTO.MenuSectionUpdateRequest;
import pos.pos.Service.Interfecaes.Menu.MenuSectionService;

import java.util.List;

@RestController
@RequestMapping(value = ApiPaths.Menu.SECTION, produces = "application/json")
@RequiredArgsConstructor
public class MenuSectionController {

    private final MenuSectionService service;

    @GetMapping
    public List<MenuSectionResponse> list(@PathVariable Long menuId) {
        return service.listSections(menuId);
    }

    @GetMapping("/{sectionId}")
    public MenuSectionResponse get(@PathVariable Long menuId,
                                   @PathVariable Long sectionId) {
        return service.getSection(menuId, sectionId);
    }

    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuSectionResponse create(@PathVariable Long menuId,
                                      @Valid @RequestBody MenuSectionCreateRequest req) {
        return service.createSection(menuId, req);
    }

    @PutMapping(value = "/{sectionId}", consumes = "application/json")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    public MenuSectionResponse update(@PathVariable Long menuId,
                                      @PathVariable Long sectionId,
                                      @Valid @RequestBody MenuSectionUpdateRequest req) {
        return service.updateSection(menuId, sectionId, req);
    }

    @DeleteMapping("/{sectionId}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long menuId,
                       @PathVariable Long sectionId) {
        service.deleteSection(menuId, sectionId);
    }

    @PatchMapping("/{sectionId}/move")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    public MenuSectionResponse move(@PathVariable Long menuId,
                                    @PathVariable Long sectionId,
                                    @RequestParam int sortOrder) {
        return service.moveSection(menuId, sectionId, sortOrder);
    }
}
