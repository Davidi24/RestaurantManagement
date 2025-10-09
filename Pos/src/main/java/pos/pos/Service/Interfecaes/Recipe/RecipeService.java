package pos.pos.Service.Interfecaes.Recipe;

import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pos.pos.DTO.Recipe.*;

import java.math.BigDecimal;

public interface RecipeService {
    RecipeResponse create(RecipeRequest request);
    RecipeResponse update(Long id, RecipeRequest request);
    void delete(Long id);
    RecipeResponse get(Long id);
    Page<RecipeResponse> list(String q, Pageable pageable);
    RecipeResponse produce(Long id, BigDecimal portions, String createdBy) throws BadRequestException;
}