package pos.pos.Repository.Notification;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import pos.pos.Entity.Notification.NotificationEvent;

import java.time.Instant;
import java.util.List;

public interface NotificationEventRepository extends JpaRepository<NotificationEvent, Long> {
    List<NotificationEvent> findTop500ByChannelAndIdGreaterThanOrderByIdAsc(String channel, Long id);

    @Modifying
    @Transactional
    void deleteByCreatedAtBefore(Instant threshold);

}
