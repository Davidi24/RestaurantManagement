package pos.pos.Config.JWT;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
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

    @Value("${app.jwt.reset-ttl-seconds:400}")
    private long resetTtlSeconds;

    private final Map<String, Instant> revokedTokens = new ConcurrentHashMap<>();

    public String createAccessToken(Authentication auth) {
        Instant now = Instant.now();
        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .collect(Collectors.toSet());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessTtlSeconds))
                .subject(auth.getName())
                .claim("roles", roles)
                .claim("purpose", "ACCESS")
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String createPasswordResetToken(Long userId) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(resetTtlSeconds))
                .subject(String.valueOf(userId))
                .claim("purpose", "PWD_RESET")
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public Long verifyPasswordResetToken(String token) {
        Jwt jwt = decoder.decode(token);
        String purpose = jwt.getClaimAsString("purpose");
        if (!"PWD_RESET".equals(purpose)) {
            throw new JwtException("Invalid token purpose");
        }
        return Long.valueOf(jwt.getSubject());
    }

    public void revokeToken(String token) {
        try {
            Jwt jwt = decoder.decode(token);
            Instant exp = jwt.getExpiresAt();
            revokedTokens.put(token, exp != null ? exp : Instant.now().plus(Duration.ofHours(1)));
        } catch (JwtException e) {
            revokedTokens.put(token, Instant.now().plus(Duration.ofHours(1)));
        }
        cleanupRevoked();
    }

    public boolean isTokenRevoked(String token) {
        Instant exp = revokedTokens.get(token);
        if (exp == null) return false;
        if (Instant.now().isAfter(exp)) {
            revokedTokens.remove(token);
            return false;
        }
        return true;
    }

    private void cleanupRevoked() {
        Instant now = Instant.now();
        revokedTokens.entrySet().removeIf(e -> now.isAfter(e.getValue()));
    }
}
