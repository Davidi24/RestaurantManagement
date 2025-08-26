// src/main/java/pos/pos/controller/MenuSectionController.java
package pos.pos.Controller.Menu;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pos.pos.DTO.Menu.MenuSectionCreateRequest;
import pos.pos.DTO.Menu.MenuSectionResponse;
import pos.pos.DTO.Menu.MenuSectionUpdateRequest;
import pos.pos.Service.Interfecaes.MenuSectionService;


import java.util.List;

@RestController
@RequestMapping("/api/menus/{menuId}/sections")
@RequiredArgsConstructor
public class MenuSectionController {

    private final MenuSectionService service;

    @GetMapping
    public List<MenuSectionResponse> list(@PathVariable Long menuId) {
        return service.listSections(menuId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuSectionResponse create(
            @PathVariable Long menuId,
            @Valid @RequestBody MenuSectionCreateRequest req
    ) {
        return service.createSection(menuId, req);
    }

    @GetMapping("/{sectionId}")
    public MenuSectionResponse get(
            @PathVariable Long menuId,
            @PathVariable Long sectionId
    ) {
        return service.getSection(menuId, sectionId);
    }

    @PutMapping("/{sectionId}")
    public MenuSectionResponse update(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @Valid @RequestBody MenuSectionUpdateRequest req
    ) {
        return service.updateSection(menuId, sectionId, req);
    }

    @DeleteMapping("/{sectionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long menuId, @PathVariable Long sectionId) {
        service.deleteSection(menuId, sectionId);
    }

    @PatchMapping("/{sectionId}/move")
    public MenuSectionResponse move(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @RequestParam int sortOrder
    ) {
        return service.moveSection(menuId, sectionId, sortOrder);
    }
}
