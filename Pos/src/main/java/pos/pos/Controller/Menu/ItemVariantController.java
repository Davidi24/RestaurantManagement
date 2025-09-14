package pos.pos.Controller.Menu;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ResponseStatus;
import pos.pos.Config.ApiPaths;
import pos.pos.DTO.Menu.VariantDTO.ItemVariantCreateRequest;
import pos.pos.DTO.Menu.VariantDTO.ItemVariantResponse;
import pos.pos.DTO.Menu.VariantDTO.ItemVariantUpdateRequest;
import pos.pos.Service.Interfecaes.Menu.ItemVariantService;

import java.util.List;

@RestController
@RequestMapping(value = ApiPaths.Menu.VARIANT, produces = "application/json")
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

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ItemVariantResponse create(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @Valid @RequestBody ItemVariantCreateRequest request
    ) {
        return service.createVariant(menuId, sectionId, itemId, request);
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
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

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{variantId}")
    public void delete(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long variantId
    ) {
        service.deleteVariant(menuId, sectionId, itemId, variantId);
    }

    @PatchMapping("/{variantId}/move-up")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    public ItemVariantResponse moveUp(@PathVariable Long menuId,
                                      @PathVariable Long sectionId,
                                      @PathVariable Long itemId,
                                      @PathVariable Long variantId) {
        return service.moveOne(menuId, sectionId, itemId, variantId, -1);
    }

    @PatchMapping("/{variantId}/move-down")
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    public ItemVariantResponse moveDown(@PathVariable Long menuId,
                                        @PathVariable Long sectionId,
                                        @PathVariable Long itemId,
                                        @PathVariable Long variantId) {
        return service.moveOne(menuId, sectionId, itemId, variantId, +1);
    }

}
