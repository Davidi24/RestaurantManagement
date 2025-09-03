package pos.pos.Config.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import pos.pos.Repository.UserRepository;

@Configuration
@RequiredArgsConstructor
public class UserDetailsConfig {

    private final UserRepository repo;

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> repo.findByEmail(username)
                .map(u -> User.withUsername(u.getEmail())
                        .password(u.getPasswordHash())
                        .authorities(u.getRoles().stream()
                                .map(r -> "ROLE_" + r.name())
                                .toArray(String[]::new))
                        .accountLocked(!u.isEnabled())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
