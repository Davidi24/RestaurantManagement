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
import pos.pos.DTO.LoginRequest;
import pos.pos.DTO.RegisterRequest;
import pos.pos.DTO.UserDto;
import pos.pos.Service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserService userService;




    @PreAuthorize("hasAnyRole('MANAGER','ADMIN','SUPERADMIN') and @roleGuard.canCreate(authentication, #req.role())")
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(userService.register(req));
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password())
        );

        String accessToken = jwtService.createAccessToken(auth);
        return ResponseEntity.ok(Map.of("accessToken", accessToken));
    }
}
