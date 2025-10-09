package pos.pos.Service.Recipe;

import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.Recipe.RecipeMapper;
import pos.pos.DTO.Recipe.*;
import pos.pos.Entity.Inventory.MovementType;
import pos.pos.Entity.Inventory.InventoryItem;
import pos.pos.Entity.Recipe.Recipe;
import pos.pos.Entity.Recipe.RecipeLine;
import pos.pos.Exeption.General.AlreadyExistsException;
import pos.pos.Exeption.General.NotFoundException;
import pos.pos.Repository.Inventory.InventoryItemRepository;
import pos.pos.Repository.Recipe.RecipeRepository;
import pos.pos.Service.Interfecaes.Inventory.InventoryService;
import pos.pos.Service.Interfecaes.Recipe.RecipeService;
import pos.pos.DTO.Inventory.MovementRequest;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecipeServiceImpl implements RecipeService {

  private final RecipeRepository repo;
  private final InventoryItemRepository itemRepo;
  private final InventoryService inventoryService;
  private final RecipeMapper mapper;

  @Override
  @Transactional
  public RecipeResponse create(RecipeRequest request) {
    String name = mapper.normalize(request.name());
    if (repo.existsByNameIgnoreCase(name)) throw new AlreadyExistsException("Recipe", name);
    Recipe saved = repo.save(mapper.toEntity(request));
    return mapper.toResponse(saved);
  }

  @Override
  @Transactional
  public RecipeResponse update(Long id, RecipeRequest request) {
    Recipe existing = repo.findById(id).orElseThrow(() -> new NotFoundException("Recipe not found"));
    String newName = mapper.normalize(request.name());
    if (!existing.getName().equalsIgnoreCase(newName) && repo.existsByNameIgnoreCase(newName))
      throw new AlreadyExistsException("Recipe", newName);
    existing.setName(newName);
    existing.setDescription(request.description());
    existing.setPortionYield(request.portionYield());
    mapper.replaceLines(existing, request.lines());
    Recipe saved = repo.save(existing);
    return mapper.toResponse(saved);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    if (!repo.existsById(id)) throw new NotFoundException("Recipe not found");
    repo.deleteById(id);
  }

  @Override
  public RecipeResponse get(Long id) {
    Recipe r = repo.findById(id).orElseThrow(() -> new NotFoundException("Recipe not found"));
    return mapper.toResponse(r);
  }

  @Override
  public Page<RecipeResponse> list(String q, Pageable pageable) {
    Page<Recipe> page = (q == null || q.isBlank()) ? repo.findAll(pageable) : repo.findAll(pageable);
    return page.map(mapper::toResponse);
  }

  @Override
  @Transactional
  public RecipeResponse produce(Long id, BigDecimal portions, String createdBy) throws BadRequestException {
    if (portions == null || portions.signum() <= 0) throw new BadRequestException("Invalid portions");
    Recipe r = repo.findById(id).orElseThrow(() -> new NotFoundException("Recipe not found"));
    for (RecipeLine l : r.getLines()) {
      BigDecimal qty = l.getQuantityPerPortion().multiply(portions);
      Optional<InventoryItem> itemOpt = itemRepo.findByIngredient_Id(l.getIngredient().getId());
      if (itemOpt.isEmpty()) throw new NotFoundException("InventoryItem not found for ingredient " + l.getIngredient().getName());
      inventoryService.move(
              itemOpt.get().getId(),
              MovementRequest.builder().type(MovementType.OUT).quantity(qty).reason("Recipe: " + r.getName()).build(),
              createdBy
      );
    }
    return mapper.toResponse(r);
  }
}
