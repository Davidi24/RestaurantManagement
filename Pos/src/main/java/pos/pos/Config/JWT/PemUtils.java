package pos.pos.Config.JWT;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

final class PemUtils {

    static PrivateKey readPrivateKey(org.springframework.core.io.Resource res) throws Exception {
        try (InputStream is = res.getInputStream()) {
            String s = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            return KeyFactory.getInstance("RSA")
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(s)));
        }
    }

    static PublicKey readPublicKey(org.springframework.core.io.Resource res) throws Exception {
        try (InputStream is = res.getInputStream()) {
            String s = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("-----\\w+ PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            return KeyFactory.getInstance("RSA")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(s)));
        }
    }
}
