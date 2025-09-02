// src/main/java/pos/pos/Service/PasswordResetService.java
package pos.pos.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pos.pos.Config.JWT.JwtService;
import pos.pos.Entity.User.PasswordResetCode;
import pos.pos.Repository.PasswordResetCodeRepository;
import pos.pos.Repository.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepo;
    private final PasswordResetCodeRepository codeRepo;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.password-reset.code-ttl-seconds:60}")
    private long ttlSeconds;

    @Value("${app.mail.from:no-reply@example.com}")
    private String from;

    @Transactional
    public void sendResetCode(String email) {
        var userOpt = userRepo.findByEmail(email);
        if (userOpt.isEmpty()) return;

        var user = userOpt.get();
        codeRepo.deleteByUser_IdAndUsedAtIsNull(user.getId());

        var code = generateCode();
        var hash = hash(user.getId() + ":" + code);
        var expires = Instant.now().plus(Duration.ofSeconds(ttlSeconds));

        var entity = PasswordResetCode.builder()
                .user(user)
                .codeHash(hash)
                .expiresAt(expires)
                .build();
        codeRepo.save(entity);

        var msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(user.getEmail());
        msg.setSubject("Your password reset code");
        msg.setText("""
                Hi %s,

                Your password reset code is: %s
                It expires in %d seconds.

                If you didn’t request this, you can ignore this email.
                """.formatted(user.getFirstName() == null ? "" : user.getFirstName(), code, ttlSeconds));
        mailSender.send(msg);
    }

    @Transactional
    public String verifyCode(String email, String code) {
        var user = userRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid code"));
        var hash = hash(user.getId() + ":" + code);
        var prc = codeRepo.findByUser_IdAndCodeHashAndUsedAtIsNull(user.getId(), hash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid code"));

        if (prc.getExpiresAt() == null || Instant.now().isAfter(prc.getExpiresAt()))
            throw new IllegalArgumentException("Code expired");

        prc.setUsedAt(Instant.now());
        return jwtService.createPasswordResetToken(user.getId());
    }

    @Transactional
    public void resetWithToken(String resetToken, String newPassword) {
        Long userId = jwtService.verifyPasswordResetToken(resetToken);
        var user = userRepo.findById(userId).orElseThrow();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        codeRepo.deleteByUser_IdAndUsedAtIsNull(userId);
    }

    private String generateCode() {
        int n = new SecureRandom().nextInt(1_000_000);
        return String.format("%06d", n);
    }

    private String hash(String value) {
        try {
            var md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
