package pos.pos.Controller.Menu;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseStatus;
import pos.pos.Config.ApiPaths;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemCreateRequest;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemResponse;
import pos.pos.DTO.Menu.MenuItemDTO.MenuItemUpdateRequest;
import pos.pos.Service.Interfecaes.Menu.MenuItemService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = ApiPaths.Menu.ITEM, produces = "application/json")
public class MenuItemController {

    private final MenuItemService service;

    @GetMapping
    public List<MenuItemResponse> list(@PathVariable Long menuId, @PathVariable Long sectionId) {
        return service.listItems(menuId, sectionId);
    }

    @GetMapping("/{itemId}")
    public MenuItemResponse getById(@PathVariable Long menuId,
                                    @PathVariable Long sectionId,
                                    @PathVariable Long itemId) {
        return service.getItems(menuId, sectionId, itemId);
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public MenuItemResponse create(@PathVariable Long menuId,
                                   @PathVariable Long sectionId,
                                   @Valid @RequestBody MenuItemCreateRequest req) {
        return service.createItem(menuId, sectionId, req);
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @PutMapping("/{itemId}")
    public MenuItemResponse update(@PathVariable Long menuId,
                                   @PathVariable Long sectionId,
                                   @PathVariable Long itemId,
                                   @Valid @RequestBody MenuItemUpdateRequest req) {
        return service.updateItem(menuId, sectionId, itemId, req);
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable Long menuId,
                       @PathVariable Long sectionId,
                       @PathVariable Long itemId) {
        service.deleteItem(menuId, sectionId, itemId);
    }

    @PatchMapping("/{itemId}/move-up")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    public MenuItemResponse moveUp(@PathVariable Long menuId,
                                   @PathVariable Long sectionId,
                                   @PathVariable Long itemId) {
        return service.moveOne(menuId, sectionId, itemId, -1);
    }

    @PatchMapping("/{itemId}/move-down")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    public MenuItemResponse moveDown(@PathVariable Long menuId,
                                     @PathVariable Long sectionId,
                                     @PathVariable Long itemId) {
        return service.moveOne(menuId, sectionId, itemId, +1);
    }
}
