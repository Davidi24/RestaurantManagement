package pos.pos.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetWithTokenRequest(
        @NotBlank String resetToken,
        @NotBlank @Size(min = 8) String newPassword
) {}
