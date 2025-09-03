package pos.pos.DTO.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyCodeRequest(
        @Email @NotBlank String email,
        @Pattern(regexp="\\d{6}") String code
) {}
