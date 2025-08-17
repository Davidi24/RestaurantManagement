package pos.pos.DTO.Mapper;

import pos.pos.DTO.UserDto;
import pos.pos.Entity.User;

public class UserMapper {
    public UserDto toDto(User u) {
        return new UserDto(
                u.getId(),
                u.getEmail(),
                u.getFirstName(),
                u.getLastName(),
                u.getRoles().stream().map(Enum::name).collect(java.util.stream.Collectors.toSet())
        );
    }

}
