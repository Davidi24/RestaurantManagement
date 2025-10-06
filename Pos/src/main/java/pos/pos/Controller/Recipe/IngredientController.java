package pos.pos.Controller.Recipe;


import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pos.pos.Config.ApiPaths;
import pos.pos.DTO.Recipe.IngredientRequest;

@RestController
@RequestMapping(ApiPaths.Ingredient.BASE)
public class IngredientController {

    @PostMapping
    public ResponseEntity<?> createIngredient(@Valid @RequestBody IngredientRequest ingredientRequest){
        return ResponseEntity.ok().body("I am here");
    }
}
