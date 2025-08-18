package pos.pos.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pos.pos.Config.JWT.JwtService;
import pos.pos.DTO.*;
import pos.pos.Service.Interfecaes.UserService;
import pos.pos.Service.PasswordResetService;


import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final PasswordResetService passwordResetService;



    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN') and @roleGuard.canCreate(authentication, #req.role())")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(userService.register(req));
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
            String accessToken = jwtService.createAccessToken(auth);
            return ResponseEntity.ok(Map.of("accessToken", accessToken));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String auth) {
        jwtService.revokeToken(auth.substring(7));
        return ResponseEntity.ok(Map.of("message", "Logged out"));
    }

    @PostMapping("/password/forgot")
    public ResponseEntity<?> forgot(@RequestParam("email") @jakarta.validation.constraints.Email @jakarta.validation.constraints.NotBlank String email) {
        passwordResetService.sendResetCode(email);
        return ResponseEntity.ok(Map.of("message", "If the email exists, a reset code was sent."));
    }

    @PostMapping("/password/verify")
    public ResponseEntity<?> verify(@Valid @RequestBody VerifyCodeRequest req) {
        String resetToken = passwordResetService.verifyCode(req.email(), req.code());
        return ResponseEntity.ok(Map.of("resetToken", resetToken));
    }

    @PostMapping("/password/reset")
    public ResponseEntity<?> reset(@Valid @RequestBody ResetWithTokenRequest req) {
        passwordResetService.resetWithToken(req.resetToken(), req.newPassword());
        return ResponseEntity.ok(Map.of("message", "Password updated"));
    }

}
