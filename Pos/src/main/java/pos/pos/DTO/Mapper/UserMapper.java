package pos.pos.DTO.Mapper;

import org.springframework.stereotype.Component;
import pos.pos.DTO.UserDto;
import pos.pos.Entity.User;

@Component
public class UserMapper {
    public UserDto toUserDTO(User u) {
        return new UserDto(
                u.getId(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                u.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())
        );
    }

}
