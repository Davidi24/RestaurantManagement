package pos.pos.DTO.Mapper.Recipe;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pos.pos.DTO.Recipe.*;
import pos.pos.Entity.Recipe.Ingredient;
import pos.pos.Entity.Recipe.Recipe;
import pos.pos.Entity.Recipe.RecipeLine;
import pos.pos.Exeption.General.NotFoundException;
import pos.pos.Repository.Recipe.IngredientRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RecipeMapper {

  private final IngredientRepository ingredientRepository;

  public Recipe toEntity(RecipeRequest req) {
    Recipe r = Recipe.builder()
            .name(normalize(req.name()))
            .description(req.description())
            .portionYield(req.portionYield())
            .build();
    List<RecipeLine> lines = new ArrayList<>();
    for (RecipeLineRequest lr : req.lines()) {
      Ingredient ing = ingredientRepository.findById(lr.ingredientId())
              .orElseThrow(() -> new NotFoundException("Ingredient not found"));
      lines.add(RecipeLine.builder()
              .recipe(r)
              .ingredient(ing)
              .quantityPerPortion(lr.quantityPerPortion())
              .build());
    }
    r.setLines(lines);
    return r;
  }

  public void replaceLines(Recipe recipe, List<RecipeLineRequest> reqLines) {
    List<RecipeLine> lines = new ArrayList<>();
    for (RecipeLineRequest lr : reqLines) {
      Ingredient ing = ingredientRepository.findById(lr.ingredientId())
              .orElseThrow(() -> new NotFoundException("Ingredient not found"));
      lines.add(RecipeLine.builder()
              .recipe(recipe)
              .ingredient(ing)
              .quantityPerPortion(lr.quantityPerPortion())
              .build());
    }
    recipe.setLines(lines);
  }

  public RecipeResponse toResponse(Recipe r) {
    BigDecimal total = BigDecimal.ZERO;
    List<RecipeLineResponse> lineDtos = new ArrayList<>();
    for (RecipeLine l : r.getLines()) {
      BigDecimal cost = l.getIngredient().getCostPerStockUnit() == null
              ? BigDecimal.ZERO
              : l.getQuantityPerPortion().multiply(l.getIngredient().getCostPerStockUnit());
      total = total.add(cost);
      lineDtos.add(RecipeLineResponse.builder()
              .id(l.getId())
              .ingredientId(l.getIngredient().getId())
              .ingredientName(l.getIngredient().getName())
              .unit(l.getIngredient().getStockUnit())
              .quantityPerPortion(l.getQuantityPerPortion())
              .costPerPortion(cost)
              .build());
    }
    return RecipeResponse.builder()
            .id(r.getId())
            .name(r.getName())
            .description(r.getDescription())
            .portionYield(r.getPortionYield())
            .totalCostPerPortion(total)
            .lines(lineDtos)
            .createdAt(r.getCreatedAt())
            .updatedAt(r.getUpdatedAt())
            .build();
  }

  public String normalize(String s) { return s == null ? null : s.trim().replaceAll("\\s+"," "); }
}