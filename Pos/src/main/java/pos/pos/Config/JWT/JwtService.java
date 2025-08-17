package pos.pos.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @Value("${app.jwt.access-ttl}")
    private long accessTtlSeconds;

    // 🔹 Create a new JWT token
    public String createAccessToken(Authentication auth) {
        Instant now = Instant.now();

        // collect roles (e.g., ROLE_USER, ROLE_ADMIN)
        var roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", "")) // store as USER/ADMIN only
                .collect(Collectors.toSet());

        // set claims
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofSeconds(accessTtlSeconds)))
                .subject(auth.getName())  // usually the email
                .claim("roles", roles)
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    // 🔹 Decode & verify token
    public Jwt parse(String token) {
        return decoder.decode(token);
    }
}
