// Repository
package pos.pos.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pos.pos.Entity.User.PasswordResetCode;

import java.util.Optional;


public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, Long> {
    long deleteByUser_IdAndUsedAtIsNull(Long userId);
    Optional<PasswordResetCode> findByUser_IdAndCodeHashAndUsedAtIsNull(Long userId, String codeHash);
}
