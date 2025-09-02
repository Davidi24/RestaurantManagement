package pos.pos.Service.Interfecaes;


import pos.pos.DTO.RegisterRequest;
import pos.pos.DTO.UserResponse;
import pos.pos.Entity.User.User;

public interface UserService {
  UserResponse register(RegisterRequest req);
  User findByEmailOrThrow(String email);
  UserResponse getByEmail(String email);
  UserResponse getById(Long id);
}
