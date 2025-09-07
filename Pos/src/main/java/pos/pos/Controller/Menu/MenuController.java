package pos.pos.Controller.Menu;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pos.pos.DTO.Menu.MenuDTO.MenuRequest;
import pos.pos.DTO.Menu.MenuDTO.MenuResponse;
import pos.pos.DTO.Menu.MenuDTO.MenuTreeResponse;
import pos.pos.Service.Interfecaes.MenuService;
import pos.pos.Config.ApiPaths;

import java.util.List;

@RestController
@RequestMapping(value = ApiPaths.Menu.BASE, produces = "application/json")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public List<MenuResponse> list() {
        return menuService.list();
    }

    @GetMapping("/{id}")
    public MenuResponse get(@PathVariable Long id) {
        return menuService.get(id);
    }

    @GetMapping("/{id}/tree")
    public MenuTreeResponse tree(@PathVariable Long id) {
        return menuService.tree(id);
    }

    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuResponse create(@Valid @RequestBody MenuRequest body) {
        return menuService.create(body);
    }

    @PatchMapping(value = "/{id}", consumes = "application/json")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    public MenuResponse patch(@PathVariable Long id, @RequestBody MenuRequest body) {
        return menuService.patch(id, body);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    public void delete(@PathVariable Long id) {
        menuService.delete(id);
    }
}
