package pos.pos.Exeption.General;

import pos.pos.Exeption.AppException;

public class NotFoundException extends AppException {
  public NotFoundException(String resourceName, String id) {
    super("The " + resourceName + " with id " + id + " was not found");
  }

  public NotFoundException(String resourceName, Long id) {
    super("The " + resourceName + " with id " + id + " was not found");
  }

  public NotFoundException(String message) {
    super(message);
  }
}
