package pos.pos.Service.Recipe;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pos.pos.Config.ApiPaths;
import pos.pos.DTO.Mapper.Recipe.IngredientMapper;
import pos.pos.DTO.Recipe.IngredientRequest;
import pos.pos.Entity.Recipe.Ingredient;
import pos.pos.Repository.Recipe.IngredientRepository;
import pos.pos.Service.Interfecaes.Recipe.IngredientService;

@Service
@AllArgsConstructor
public class IngredientServiceImpl implements IngredientService {
    private final IngredientRepository ingredientRepository;
    private final IngredientMapper ingredientMapper;

    @Override
    public Object addIngredient(IngredientRequest ingredientRequest) {
        Ingredient ingredient = ingredientMapper.toIngredient(ingredientRequest);
        return ingredientRepository.save(ingredient);
    }
}
