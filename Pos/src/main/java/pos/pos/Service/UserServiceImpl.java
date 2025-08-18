package pos.pos.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.DTO.Mapper.UserMapper;
import pos.pos.DTO.RegisterRequest;
import pos.pos.DTO.UserResponse;
import pos.pos.Entity.User;
import pos.pos.Exeption.EmailAlreadyUsedException;
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
      throw new EmailAlreadyUsedException(req.email());
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
