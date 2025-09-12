package pos.pos.Controller.Notification;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pos.pos.Service.Notification.SseHub;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/sse")
@RequiredArgsConstructor
public class SseController {
    private final SseHub hub;

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(Authentication auth,
                             @RequestHeader(value = "Last-Event-ID", required = false) String lastId) {
        var roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        Long after = null;
        try { if (lastId != null) after = Long.parseLong(lastId.trim()); } catch (NumberFormatException ignored) {}

        if (roles.contains("ROLE_SUPERADMIN")) {
            return hub.subscribeMulti(Set.of(
                    "role:KITCHEN","role:MANAGER","role:ADMIN","role:WAITER","role:USER"
            ), after);
        }

        String channel =
                roles.contains("ROLE_KITCHEN")   ? "role:KITCHEN" :
                        roles.contains("ROLE_MANAGER")   ? "role:MANAGER" :
                                roles.contains("ROLE_ADMIN")     ? "role:ADMIN"   :
                                        roles.contains("ROLE_WAITER")    ? "role:WAITER"  :
                                                roles.contains("ROLE_USER")      ? "role:USER"    : "role:DEFAULT";

        return hub.subscribe(channel, after);
    }

    @PostMapping("/publish")
    public void publish(Authentication auth, @RequestBody Map<String, Object> payload) {
        var roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        boolean allowed = roles.contains("ROLE_SUPERADMIN") || roles.contains("ROLE_ADMIN") || roles.contains("ROLE_MANAGER");
        if (!allowed) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed to publish events");

        String channel = (String) payload.get("channel");
        String event   = (String) payload.get("event");
        Object data    = payload.get("data");
        hub.publish(channel, event, data);
    }
}
