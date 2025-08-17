package pos.pos.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pos.pos.DTO.Mapper.UserMapper;
import pos.pos.DTO.RegisterRequest;
import pos.pos.DTO.UserDto;
import pos.pos.Entity.Role;
import pos.pos.Entity.User;
import pos.pos.Exeption.EmailAlreadyUsedException;
import pos.pos.Repository.UserRepository;


import java.nio.file.AccessDeniedException;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository repo;
  private final PasswordEncoder encoder;
  private final UserMapper userMapper;


  @Transactional
  @Override
  public UserDto register(RegisterRequest req) {
    if (repo.existsByEmail(req.email()))
      throw new EmailAlreadyUsedException(req.email());

    var u = User.builder()
            .email(req.email())
            .passwordHash(encoder.encode(req.password()))
            .firstName(req.firstName())
            .lastName(req.lastName())
            .roles(Set.of(req.role()))
            .build();
    u = repo.save(u);
    return userMapper.toUserDTO(u);
  }

  @Override
  public User findByEmailOrThrow(String email) {
    return repo.findByEmail(email).orElseThrow();
  }

  @Override
  public UserDto getByEmail(String email) {
    User u = repo.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
    return userMapper.toUserDTO(u);
  }

  @Override
  public UserDto getById(Long id) {
    User u = repo.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    return userMapper.toUserDTO(u);
  }


}
