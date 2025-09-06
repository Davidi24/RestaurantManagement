package pos.pos.Controller.Menu;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pos.pos.Config.ApiPaths;
import pos.pos.DTO.Menu.OptionDTO.OptionItemCreateRequest;
import pos.pos.DTO.Menu.OptionDTO.OptionItemResponse;
import pos.pos.DTO.Menu.OptionDTO.OptionItemUpdateRequest;
import pos.pos.Service.Interfecaes.OptionItemService;

import java.util.List;

@RestController
@RequestMapping(
        value = ApiPaths.Menu.OPTION_ITEM,
        produces = "application/json"
)
@RequiredArgsConstructor
public class OptionItemController {

    private final OptionItemService service;

    @GetMapping
    public List<OptionItemResponse> list(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId
    ) {
        return service.list(menuId, sectionId, itemId, groupId);
    }

    @GetMapping("/{optionId}")
    public OptionItemResponse get(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId,
            @PathVariable Long optionId
    ) {
        return service.get(menuId, sectionId, itemId, groupId, optionId);
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = "application/json")
    public OptionItemResponse create(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId,
            @Valid @RequestBody OptionItemCreateRequest body
    ) {
        return service.create(menuId, sectionId, itemId, groupId, body);
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @PatchMapping(value = "/{optionId}", consumes = "application/json")
    public OptionItemResponse patch(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId,
            @PathVariable Long optionId,
            @Valid @RequestBody OptionItemUpdateRequest body
    ) {
        return service.patch(menuId, sectionId, itemId, groupId, optionId, body);
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{optionId}")
    public void delete(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId,
            @PathVariable Long optionId
    ) {
        service.delete(menuId, sectionId, itemId, groupId, optionId);
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @PatchMapping("/{optionId}/move-up")
    public OptionItemResponse moveUp(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId,
            @PathVariable Long optionId
    ) {
        return service.moveOne(menuId, sectionId, itemId, groupId, optionId, -1);
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @PatchMapping("/{optionId}/move-down")
    public OptionItemResponse moveDown(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId,
            @PathVariable Long optionId
    ) {
        return service.moveOne(menuId, sectionId, itemId, groupId, optionId, +1);
    }
}
