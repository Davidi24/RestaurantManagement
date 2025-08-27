package pos.pos.Controller.Menu;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupCreateRequest;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupResponse;
import pos.pos.DTO.Menu.OptionDTO.OptionGroupUpdateRequest;
import pos.pos.Service.Interfecaes.OptionGroupService;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(
        value = "/menus/{menuId}/sections/{sectionId}/items/{itemId}/option-groups",
        produces = "application/json"
)
public class OptionGroupController {

    private final OptionGroupService service;

    @GetMapping
    public ResponseEntity<List<OptionGroupResponse>> list(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId
    ) {
        var result = service.listByItem(menuId, sectionId, itemId);
        return ResponseEntity.ok(result);
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<OptionGroupResponse> create(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @Valid @RequestBody OptionGroupCreateRequest body
    ) {
        var created = service.create(menuId, sectionId, itemId, body);

        // Location: /menus/{menuId}/sections/{sectionId}/items/{itemId}/option-groups/{id}
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{groupId}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<OptionGroupResponse> getOne(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId
    ) {
        var result = service.getById(menuId, sectionId, itemId, groupId);
        return ResponseEntity.ok(result);
    }

    @PatchMapping(value = "/{groupId}", consumes = "application/json")
    public ResponseEntity<OptionGroupResponse> patch(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId,
            @Valid @RequestBody OptionGroupUpdateRequest body
    ) {
        var updated = service.patch(menuId, sectionId, itemId, groupId, body);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId
    ) {
        service.delete(menuId, sectionId, itemId, groupId);
        return ResponseEntity.noContent().build();
    }
}
