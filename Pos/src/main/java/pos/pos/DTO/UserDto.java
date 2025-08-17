package pos.pos.DTO;

import java.util.Set;

public record UserDto(
  Long id,
  String email,
  String firstName,
  String lastName,
  Set<String> roles
) {}
