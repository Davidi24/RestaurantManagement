package pos.pos.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pos.pos.Entity.User.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
  boolean existsByEmail(String email);
}
