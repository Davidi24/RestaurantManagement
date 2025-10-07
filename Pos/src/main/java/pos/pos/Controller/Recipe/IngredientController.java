package pos.pos.Controller.Recipe;


import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pos.pos.Config.ApiPaths;
import pos.pos.DTO.Recipe.IngredientRequest;
import pos.pos.Service.Interfecaes.Recipe.IngredientService;

@RestController
@RequestMapping(ApiPaths.Ingredient.BASE)
@AllArgsConstructor
public class IngredientController {

    private final IngredientService ingredientService;

    @PostMapping
    public ResponseEntity<?> createIngredient(@Valid @RequestBody IngredientRequest ingredientRequest){
        return ResponseEntity.ok().body(ingredientService.addIngredient(ingredientRequest));
    }
}
