package pos.pos.Config.JWT;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.interfaces.RSAPublicKey;

@Configuration
public class JwtConfig {

    @Value("${app.jwt.private-key-path}")
    private Resource privateKeyRes;

    @Value("${app.jwt.public-key-path}")
    private Resource publicKeyRes;

    @Bean
    @SneakyThrows
    public JwtEncoder jwtEncoder() {
        var priv = PemUtils.readPrivateKey(privateKeyRes);
        var pub  = (RSAPublicKey) PemUtils.readPublicKey(publicKeyRes);
        var jwk = new RSAKey.Builder(pub).privateKey(priv).keyID("key-1").build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }

    @Bean
    @SneakyThrows
    public JwtDecoder jwtDecoder() {
        var pub = (RSAPublicKey) PemUtils.readPublicKey(publicKeyRes);
        return NimbusJwtDecoder.withPublicKey(pub).build();
    }
}
