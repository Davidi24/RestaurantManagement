package pos.pos.DTO.Mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pos.pos.DTO.User.RegisterRequest;
import pos.pos.DTO.User.UserResponse;
import pos.pos.Entity.User.Role;
import pos.pos.Entity.User.User;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder encoder;

    public UserResponse toUserDTO(User u) {
        return new UserResponse(
                u.getId(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                u.getRoles().stream()
                        .map(Enum::name) // Enum → String
                        .collect(Collectors.toSet())
        );
    }

    public User toUser(RegisterRequest r) {
        Role role = Role.valueOf(r.role().trim().toUpperCase());
        return User.builder()
                .email(r.email())
                .passwordHash(encoder.encode(r.password()))
                .firstName(r.firstName())
                .lastName(r.lastName())
                .roles(Set.of(role))
                .enabled(true)
                .build();
    }

}
