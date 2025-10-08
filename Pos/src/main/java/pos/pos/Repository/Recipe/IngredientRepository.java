package pos.pos.Repository.Recipe;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pos.pos.Entity.Recipe.Ingredient;

import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Ingredient> findByNameIgnoreCase(String name);
    Page<Ingredient> findByNameContainingIgnoreCase(String name, Pageable pageable);
}