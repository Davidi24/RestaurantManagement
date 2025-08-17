package pos.pos.Validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pos.pos.Repository.UserRepository;

@Component
@RequiredArgsConstructor
public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
  private final UserRepository repo;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext ctx) {
    if (value == null) return true;
    return !repo.existsByEmail(value);
  }
}
