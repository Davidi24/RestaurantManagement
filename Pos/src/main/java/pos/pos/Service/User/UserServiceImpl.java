package pos.pos.Service.User;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.UserMapper;
import pos.pos.DTO.User.RegisterRequest;
import pos.pos.DTO.User.UserResponse;
import pos.pos.Entity.User.User;
import pos.pos.Exeption.AlreadyExistsException;
import pos.pos.Repository.UserRepository;
import pos.pos.Service.Interfecaes.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository repo;
  private final PasswordEncoder encoder;
  private final UserMapper userMapper;


  @Transactional
  @Override
  public UserResponse register(RegisterRequest req) {
    if (repo.existsByEmail(req.email())) {
      throw new AlreadyExistsException("Email",req.email());
    }
    User u = userMapper.toUser(req);
    u = repo.save(u);
    return userMapper.toUserDTO(u);
  }

  @Override
  public User findByEmailOrThrow(String email) {
    return repo.findByEmail(email).orElseThrow();
  }

  @Override
  public UserResponse getByEmail(String email) {
    User u = repo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    return userMapper.toUserDTO(u);
  }

  @Override
  public UserResponse getById(Long id) {
    User u = repo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    return userMapper.toUserDTO(u);
  }


}
