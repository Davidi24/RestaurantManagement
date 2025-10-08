package pos.pos.DTO.Mapper.Recipe;

import org.springframework.stereotype.Component;
import pos.pos.DTO.Recipe.IngredientRequest;
import pos.pos.DTO.Recipe.IngredientResponse;
import pos.pos.Entity.Recipe.Ingredient;

@Component
public class IngredientMapper {

    public Ingredient toEntity(IngredientRequest req) {
        return Ingredient.builder()
                .name(normalize(req.name()))
                .stockUnit(req.stockUnit())
                .costPerStockUnit(req.costPerStockUnit())
                .allergenInfo(req.allergenInfo())
                .build();
    }

    public void updateEntity(Ingredient e, IngredientRequest req) {
        e.setName(normalize(req.name()));
        e.setStockUnit(req.stockUnit());
        e.setCostPerStockUnit(req.costPerStockUnit());
        e.setAllergenInfo(req.allergenInfo());
    }

    public IngredientResponse toResponse(Ingredient e) {
        return IngredientResponse.builder()
                .id(e.getId())
                .name(e.getName())
                .stockUnit(e.getStockUnit())
                .costPerStockUnit(e.getCostPerStockUnit())
                .allergenInfo(e.getAllergenInfo())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public String normalize(String s) {
        return s == null ? null : s.trim().replaceAll("\\s+", " ");
    }
}
