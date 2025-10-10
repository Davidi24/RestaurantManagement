package pos.pos.Service.Recipe;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.Recipe.IngredientMapper;
import pos.pos.DTO.Recipe.Ingredient.IngredientRequest;
import pos.pos.DTO.Recipe.Ingredient.IngredientResponse;
import pos.pos.DTO.Recipe.Ingredient.IngredientUpdateRequest;
import pos.pos.Entity.Recipe.Ingredient;
import pos.pos.Exeption.General.AlreadyExistsException;
import pos.pos.Exeption.General.NotFoundException;
import pos.pos.Repository.Recipe.IngredientRepository;
import pos.pos.Service.Interfecaes.Recipe.IngredientService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository repo;
    private final IngredientMapper mapper;

    @Override
    @Transactional
    public IngredientResponse create(IngredientRequest request) {
        String name = mapper.normalize(request.name());
        if (repo.existsByNameIgnoreCase(name)) {
            throw new AlreadyExistsException("Ingredient", name);
        }
        try {
            Ingredient saved = repo.saveAndFlush(mapper.toEntity(request));
            return mapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyExistsException("Ingredient", name);
        }
    }

    @Override
    @Transactional
    public IngredientResponse update(Long id, IngredientRequest request) {
        Ingredient existing = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));

        String newName = mapper.normalize(request.name());
        if (!existing.getName().equalsIgnoreCase(newName)
                && repo.existsByNameIgnoreCase(newName)) {
            throw new AlreadyExistsException("Ingredient", newName);
        }

        mapper.updateEntity(existing, request);
        Ingredient saved = repo.saveAndFlush(existing);

        return mapper.toResponse(saved);
    }

    @Transactional
    public IngredientResponse partialUpdate(Long id, IngredientUpdateRequest req) {
        Ingredient existing = repo.findById(id)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));

        if (req.name() != null && !req.name().isBlank())
            existing.setName(mapper.normalize(req.name()));

        if (req.stockUnit() != null)
            existing.setStockUnit(req.stockUnit());

        if (req.costPerStockUnit() != null)
            existing.setCostPerStockUnit(req.costPerStockUnit());

        if (req.allergenInfo() != null)
            existing.setAllergenInfo(req.allergenInfo());

        return mapper.toResponse(repo.save(existing));
    }


    @Override
    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new NotFoundException("Ingredient not found");
        }
        repo.deleteById(id);
    }

    @Override
    public IngredientResponse get(Long id) {
        return repo.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Ingredient not found"));
    }

    //??
    @Override
    public Page<IngredientResponse> list(String q, Pageable pageable) {
        String query = q == null ? null : q.trim();
        Page<Ingredient> page = (query == null || query.isEmpty())
                ? repo.findAll(pageable)
                : repo.findByNameContainingIgnoreCase(query, pageable);
        return page.map(mapper::toResponse);
    }
}
