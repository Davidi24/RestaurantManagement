package pos.pos.Controller.Menu;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pos.pos.DTO.Menu.OptionDTO.OptionItemCreateRequest;
import pos.pos.DTO.Menu.OptionDTO.OptionItemResponse;
import pos.pos.DTO.Menu.OptionDTO.OptionItemUpdateRequest;
import pos.pos.Service.Interfecaes.OptionItemService;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(
        value = "/menus/{menuId}/sections/{sectionId}/items/{itemId}/option-groups/{groupId}/options",
        produces = "application/json"
)
public class OptionItemController {

    private final OptionItemService service;

    @GetMapping
    public ResponseEntity<List<OptionItemResponse>> list(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId
    ) {
        return ResponseEntity.ok(service.list(menuId, sectionId, itemId, groupId));
    }

    @PostMapping(consumes = "application/json")
    public ResponseEntity<OptionItemResponse> create(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId,
            @Valid @RequestBody OptionItemCreateRequest body
    ) {
        var created = service.create(menuId, sectionId, itemId, groupId, body);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{optionId}")
                .buildAndExpand(created.id())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{optionId}")
    public ResponseEntity<OptionItemResponse> get(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId,
            @PathVariable Long optionId
    ) {
        return ResponseEntity.ok(service.get(menuId, sectionId, itemId, groupId, optionId));
    }

    @PatchMapping(value = "/{optionId}", consumes = "application/json")
    public ResponseEntity<OptionItemResponse> patch(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId,
            @PathVariable Long optionId,
            @Valid @RequestBody OptionItemUpdateRequest body
    ) {
        return ResponseEntity.ok(service.patch(menuId, sectionId, itemId, groupId, optionId, body));
    }

    @DeleteMapping("/{optionId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long menuId,
            @PathVariable Long sectionId,
            @PathVariable Long itemId,
            @PathVariable Long groupId,
            @PathVariable Long optionId
    ) {
        service.delete(menuId, sectionId, itemId, groupId, optionId);
        return ResponseEntity.noContent().build();
    }
}
