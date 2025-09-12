package pos.pos.Util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pos.pos.Service.Notification.SseHub;

@Component
@RequiredArgsConstructor
public class NotificationSender {

    private final SseHub hub;

    public void sendMessage(String event, Object data, String... roles) {
        for (String r : roles) {
            hub.publish("role:" + r, event, data);
        }
    }
}
