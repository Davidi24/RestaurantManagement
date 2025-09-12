// pos.pos.Config.Security.RoleGuard
package pos.pos.Config.Security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import pos.pos.Entity.User.UserRole;

@Component
public class RoleGuard {

    public boolean canCreate(Authentication auth, UserRole targetRole) {
        boolean isSuperAdmin = has(auth, "ROLE_SUPERADMIN");
        boolean isAdmin      = has(auth, "ROLE_ADMIN");
        boolean isManager    = has(auth, "ROLE_MANAGER");

        if (isSuperAdmin) {
            return true;
        }
        if (isAdmin) {
            return targetRole == UserRole.MANAGER
                    || targetRole == UserRole.WAITER
                    || targetRole == UserRole.KITCHEN
                    || targetRole == UserRole.USER;
        }
        if (isManager) {
            return targetRole == UserRole.WAITER
                    || targetRole == UserRole.KITCHEN
                    || targetRole == UserRole.USER;
        }
        return false;
    }

    private boolean has(Authentication a, String role) {
        return a.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role::equals);
    }
}
