package pos.pos.Service.Interfecaes.User;


import pos.pos.DTO.User.RegisterRequest;
import pos.pos.DTO.User.UserResponse;
import pos.pos.Entity.User.User;

public interface UserService {
  UserResponse register(RegisterRequest req);
  User findByEmailOrThrow(String email);
  UserResponse getByEmail(String email);
  UserResponse getById(Long id);
}
