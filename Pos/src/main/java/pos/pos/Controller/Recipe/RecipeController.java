package pos.pos.Controller.Recipe;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pos.pos.DTO.Recipe.*;
import pos.pos.Service.Interfecaes.Recipe.RecipeService;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/recipes")
@RequiredArgsConstructor
public class RecipeController {

  private final RecipeService service;

  @PostMapping
  public ResponseEntity<RecipeResponse> create(@Valid @RequestBody RecipeRequest req) {
    return ResponseEntity.ok(service.create(req));
  }

  @PutMapping("/{id}")
  public ResponseEntity<RecipeResponse> update(@PathVariable Long id, @Valid @RequestBody RecipeRequest req) {
    return ResponseEntity.ok(service.update(id, req));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<RecipeResponse> get(@PathVariable Long id) {
    return ResponseEntity.ok(service.get(id));
  }

  @GetMapping
  public ResponseEntity<Page<RecipeResponse>> list(@RequestParam(value="q", required=false) String q,
                                                   @RequestParam(value="page", defaultValue="0") int page,
                                                   @RequestParam(value="size", defaultValue="20") int size) {
    return ResponseEntity.ok(service.list(q, PageRequest.of(page, size)));
  }

  @PostMapping("/{id}/produce")
  public ResponseEntity<RecipeResponse> produce(@PathVariable Long id,
                                                @RequestParam("portions") BigDecimal portions,
                                                @RequestHeader(value="X-User", required=false) String user) throws BadRequestException {
    return ResponseEntity.ok(service.produce(id, portions, user));
  }
}
