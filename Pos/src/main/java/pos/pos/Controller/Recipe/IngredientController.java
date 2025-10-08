package pos.pos.Controller.Recipe;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pos.pos.Config.ApiPaths;
import pos.pos.DTO.Recipe.IngredientRequest;
import pos.pos.DTO.Recipe.IngredientResponse;
import pos.pos.DTO.Recipe.IngredientUpdateRequest;
import pos.pos.Service.Interfecaes.Recipe.IngredientService;

@RestController
@RequestMapping(ApiPaths.Ingredient.BASE)
@RequiredArgsConstructor
public class IngredientController {

    private final IngredientService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    public ResponseEntity<IngredientResponse> create(@Valid @RequestBody IngredientRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    public ResponseEntity<IngredientResponse> update(@PathVariable Long id, @Valid @RequestBody IngredientRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    public ResponseEntity<IngredientResponse> patch(
            @PathVariable Long id,
            @RequestBody IngredientUpdateRequest req) {
        return ResponseEntity.ok(service.partialUpdate(id, req));
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<IngredientResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.get(id));
    }

    @GetMapping
    public ResponseEntity<Page<IngredientResponse>> list(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return ResponseEntity.ok(service.list(q, PageRequest.of(page, size)));
    }
}
