package pos.pos.Repository.Recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pos.pos.Entity.Recipe.Ingredient;


@Repository
public interface  IngredientRepository extends JpaRepository<Ingredient, Long> {
}
