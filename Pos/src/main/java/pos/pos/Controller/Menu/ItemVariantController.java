package pos.pos.Controller.Menu;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import pos.pos.DTO.Menu.VariantDTO.ItemVariantCreateRequest;
import pos.pos.DTO.Menu.VariantDTO.ItemVariantResponse;
import pos.pos.DTO.Menu.VariantDTO.ItemVariantUpdateRequest;
import pos.pos.Service.Interfecaes.ItemVariantService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/menus/{menuId}/sections/{sectionId}/items/{itemId}/variants")
@RequiredArgsConstructor
public class ItemVariantController {

    private final ItemVariantService service;

    @GetMapping
    public List<ItemVariantResponse> list(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId
    ) {
        return service.listVariants(menuId, sectionId, itemId);
    }

    @GetMapping("/{variantId}")
    public ItemVariantResponse getOne(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long variantId
    ) {
        return service.getVariant(menuId, sectionId, itemId, variantId);
    }

    @PostMapping
    public ResponseEntity<ItemVariantResponse> create(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @Valid @RequestBody ItemVariantCreateRequest request,
            UriComponentsBuilder uriBuilder
    ) {
        ItemVariantResponse created = service.createVariant(menuId, sectionId, itemId, request);
        URI location = uriBuilder
                .path("/menus/{menuId}/sections/{sectionId}/items/{itemId}/variants/{variantId}")
                .buildAndExpand(menuId, sectionId, itemId, created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{variantId}")
    public ItemVariantResponse update(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long variantId,
            @Valid @RequestBody ItemVariantUpdateRequest request
    ) {
        return service.updateVariant(menuId, sectionId, itemId, variantId, request);
    }

    @DeleteMapping("/{variantId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long variantId
    ) {
        service.deleteVariant(menuId, sectionId, itemId, variantId);
        return ResponseEntity.noContent().build();
    }
}
