package pos.pos.Controller.Menu;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pos.pos.Config.ApiPaths;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupCreateRequest;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupResponse;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupUpdateRequest;
import pos.pos.Service.Interfecaes.Menu.OptionGroupService;

import java.util.List;

@RestController
@RequestMapping(
        value = ApiPaths.Menu.OPTION_GROUP,
        produces = "application/json"
)
@RequiredArgsConstructor
public class OptionGroupController {

    private final OptionGroupService service;

    @GetMapping
    public List<OptionGroupResponse> list(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId
    ) {
        return service.listByItem(menuId, sectionId, itemId);
    }

    @GetMapping("/{groupId}")
    public OptionGroupResponse getOne(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId
    ) {
        return service.getById(menuId, sectionId, itemId, groupId);
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = "application/json")
    public OptionGroupResponse create(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @Valid @RequestBody OptionGroupCreateRequest body
    ) {
        return service.create(menuId, sectionId, itemId, body);
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @PatchMapping(value = "/{groupId}", consumes = "application/json")
    public OptionGroupResponse patch(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId,
            @Valid @RequestBody OptionGroupUpdateRequest body
    ) {
        return service.patch(menuId, sectionId, itemId, groupId, body);
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{groupId}")
    public void delete(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId
    ) {
        service.delete(menuId, sectionId, itemId, groupId);
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @PatchMapping("/{groupId}/move-up")
    public OptionGroupResponse moveUp(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId
    ) {
        return service.moveOne(menuId, sectionId, itemId, groupId, -1);
    }

    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN')")
    @PatchMapping("/{groupId}/move-down")
    public OptionGroupResponse moveDown(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId
    ) {
        return service.moveOne(menuId, sectionId, itemId, groupId, +1);
    }
}
