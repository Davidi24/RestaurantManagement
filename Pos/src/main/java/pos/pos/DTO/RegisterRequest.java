package pos.pos.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pos.pos.Entity.Role;
import pos.pos.Validation.ValidEnum;

public record RegisterRequest(
        @Email(message = "Email is not valid")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password is required")
        String password,

        @NotBlank(message = "First name is required")
        String firstName,

        String lastName,

        @NotNull(message = "Role must be provided")
        @ValidEnum(enumClass = Role.class, message = "Role does not exist")
        String role
) {}
