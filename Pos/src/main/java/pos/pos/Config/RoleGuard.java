// pos.pos.Config.RoleGuard
package pos.pos.Config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import pos.pos.Entity.Role;

@Component
public class RoleGuard {
    public boolean canCreate(Authentication auth, Role targetRole) {
        boolean isSuperAdmin = has(auth, "ROLE_SUPERADMIN");
        boolean isAdmin      = has(auth, "ROLE_ADMIN");
        boolean isManager    = has(auth, "ROLE_MANAGER");

        if (isSuperAdmin) {
            return true;
        }
        if (isAdmin) {
            return targetRole == Role.MANAGER
                    || targetRole == Role.WAITER
                    || targetRole == Role.KITCHEN
                    || targetRole == Role.USER;
        }
        if (isManager) {
            return targetRole == Role.WAITER
                    || targetRole == Role.KITCHEN
                    || targetRole == Role.USER;
        }
        return false;
    }

    private boolean has(Authentication a, String role) {
        return a.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}
