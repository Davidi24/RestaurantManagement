package pos.pos.Service.Interfecaes.Recipe;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pos.pos.DTO.Recipe.Ingredient.IngredientRequest;
import pos.pos.DTO.Recipe.Ingredient.IngredientResponse;
import pos.pos.DTO.Recipe.Ingredient.IngredientUpdateRequest;

public interface IngredientService {
    IngredientResponse create(IngredientRequest request);
    IngredientResponse update(Long id, IngredientRequest request);
    IngredientResponse partialUpdate(Long id, IngredientUpdateRequest req);
    void delete(Long id);
    IngredientResponse get(Long id);
    Page<IngredientResponse> list(String q, Pageable pageable);
}
