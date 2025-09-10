package pos.pos.Repository.Notification;

import org.springframework.data.jpa.repository.JpaRepository;
import pos.pos.Entity.Notification.NotificationEvent;
import java.util.List;

public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {
    List<NotificationEvent> findTop500ByChannelAndIdGreaterThanOrderByIdAsc(String channel, Long afterId);
}
