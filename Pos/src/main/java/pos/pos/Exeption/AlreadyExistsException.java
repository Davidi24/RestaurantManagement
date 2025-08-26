package pos.pos.Exeption;

public class AlreadyExistsException extends AppException {
  public AlreadyExistsException(String resourceName, String value) {
    super(resourceName + " already exists: " + value);
  }
}
