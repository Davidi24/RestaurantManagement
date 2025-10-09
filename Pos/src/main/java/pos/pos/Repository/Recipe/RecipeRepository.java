package pos.pos.Repository.Recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import pos.pos.Entity.Recipe.Recipe;

import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Long> {
  boolean existsByNameIgnoreCase(String name);
  Optional<Recipe> findByNameIgnoreCase(String name);
}