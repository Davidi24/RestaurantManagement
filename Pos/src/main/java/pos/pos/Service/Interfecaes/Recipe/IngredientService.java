package pos.pos.Service.Interfecaes.Recipe;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pos.pos.DTO.Recipe.IngredientRequest;
import pos.pos.DTO.Recipe.IngredientResponse;
import pos.pos.DTO.Recipe.IngredientUpdateRequest;

public interface IngredientService {
    IngredientResponse create(IngredientRequest request);
    IngredientResponse update(Long id, IngredientRequest request);
    IngredientResponse partialUpdate(Long id, IngredientUpdateRequest req);
    void delete(Long id);
    IngredientResponse get(Long id);
    Page<IngredientResponse> list(String q, Pageable pageable);
}
