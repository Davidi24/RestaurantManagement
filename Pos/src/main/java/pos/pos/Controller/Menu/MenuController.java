package pos.pos.Controller.Menu;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pos.pos.DTO.MenuRequest;
import pos.pos.DTO.MenuResponse;
import pos.pos.DTO.MenuTreeResponse;
import pos.pos.Service.Interfecaes.MenuService;


import java.util.List;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @PostMapping
    public ResponseEntity<MenuResponse> create(@RequestBody MenuRequest body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(menuService.create(body));
    }

    @GetMapping
    public List<MenuResponse> list() {
        return menuService.list();
    }

    @GetMapping("/{id}")
    public MenuResponse get(@PathVariable Long id) {
        return menuService.get(id);
    }

    @PatchMapping("/{id}")
    public MenuResponse patch(@PathVariable Long id, @RequestBody MenuRequest body) {
        return menuService.patch(id, body);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @GetMapping("/{id}/tree")
    public MenuTreeResponse tree(@PathVariable Long id) {
        return menuService.tree(id);
    }
}
