package pos.pos.Exeption.General;

import pos.pos.Exeption.AppException;

public class AlreadyExistsException extends AppException {
  public AlreadyExistsException(String resourceName, String value) {
    super(resourceName + " already exists: " + value);
  }

  public AlreadyExistsException(String resourceName, Long id) {
    super("The " + resourceName + " with id " + id + " already exists");
  }

  public AlreadyExistsException(String message) {
    super(message);
  }
}
