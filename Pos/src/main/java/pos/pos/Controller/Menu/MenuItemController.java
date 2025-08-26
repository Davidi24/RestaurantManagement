package pos.pos.Controller.Menu;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pos.pos.DTO.*;
import pos.pos.DTO.Menu.MenuItemCreateRequest;
import pos.pos.DTO.Menu.MenuItemResponse;
import pos.pos.DTO.Menu.MenuItemUpdateRequest;
import pos.pos.Service.Interfecaes.MenuItemService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/menus/{menuId}/sections/{sectionId}/items")
public class MenuItemController {

    private final MenuItemService service;

    @GetMapping
    public List<MenuItemResponse> list(@PathVariable Long menuId, @PathVariable Long sectionId) {
        return service.listItems(menuId, sectionId);
    }

    @PostMapping
    public MenuItemResponse create(@PathVariable Long menuId,
                                   @PathVariable Long sectionId,
                                   @Valid @RequestBody MenuItemCreateRequest req) {
        return service.createItem(menuId, sectionId, req);
    }

    @PutMapping("/{itemId}")
    public MenuItemResponse update(@PathVariable Long menuId,
                                   @PathVariable Long sectionId,
                                   @PathVariable Long itemId,
                                   @Valid @RequestBody MenuItemUpdateRequest req) {
        return service.updateItem(menuId, sectionId, itemId, req);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@PathVariable Long menuId,
                       @PathVariable Long sectionId,
                       @PathVariable Long itemId) {
        service.deleteItem(menuId, sectionId, itemId);
    }

    @PatchMapping("/{itemId}/move")
    public MenuItemResponse move(@PathVariable Long menuId,
                                 @PathVariable Long sectionId,
                                 @PathVariable Long itemId,
                                 @RequestParam int newSortOrder) { // 1-based target
        return service.moveItem(menuId, sectionId, itemId, newSortOrder);
    }
}
