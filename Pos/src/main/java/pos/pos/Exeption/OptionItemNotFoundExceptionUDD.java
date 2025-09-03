// 404 – option item not found
package pos.pos.Exeption;

import java.util.UUID;

public class OptionItemNotFoundExceptionUDD extends RuntimeException {
  public OptionItemNotFoundExceptionUDD(UUID publicId) {
    super("OptionItem not found for publicId=" + publicId);
  }
}
