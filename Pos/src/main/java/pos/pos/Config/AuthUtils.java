package pos.pos.Config;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AuthUtils {

    public String getUserEmail(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new AuthenticationCredentialsNotFoundException("No valid JWT authentication found");
        }

        String email = jwtAuth.getToken().getSubject();
        if (!StringUtils.hasText(email)) {
            throw new AuthenticationCredentialsNotFoundException("JWT subject (email) is missing or empty");
        }

        return email;
    }

    public Long getUserId(Authentication authentication) {
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new AuthenticationCredentialsNotFoundException("No valid JWT authentication found");
        }

        Object claim = jwtAuth.getToken().getClaims().get("userId");
        if (claim instanceof Number n) {
            return n.longValue();
        }
        if (claim instanceof String s && StringUtils.hasText(s)) {
            try {
                return Long.valueOf(s);
            } catch (NumberFormatException ignored) {}
        }

        throw new AuthenticationCredentialsNotFoundException("No valid userId claim in token");
    }
}
