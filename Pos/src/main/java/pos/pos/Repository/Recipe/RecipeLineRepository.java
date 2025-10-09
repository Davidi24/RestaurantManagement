package pos.pos.Repository.Recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import pos.pos.Entity.Recipe.RecipeLine;

public interface RecipeLineRepository extends JpaRepository<RecipeLine, Long> { }