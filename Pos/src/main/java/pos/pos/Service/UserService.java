package pos.pos.Service;


import pos.pos.DTO.RegisterRequest;
import pos.pos.DTO.UserDto;
import pos.pos.Entity.User;

import java.nio.file.AccessDeniedException;

public interface UserService {
  UserDto register(RegisterRequest req);
  User findByEmailOrThrow(String email);
  UserDto getByEmail(String email);
  UserDto getById(Long id);
}
