package pos.pos.Exeption;

public class EmailAlreadyUsedException extends AppException {
  public EmailAlreadyUsedException(String email) {
    super("Email already in use: " + email);
  }
}
