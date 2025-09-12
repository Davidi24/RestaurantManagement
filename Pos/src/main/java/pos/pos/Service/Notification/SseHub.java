package pos.pos.Service.Notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pos.pos.Repository.Notification.NotificationEventRepository;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class SseHub {
    private static final long TIMEOUT = Duration.ofMinutes(30).toMillis();
    private static final int PAGE_SIZE = 500;
    private static final Set<String> KNOWN_ROLES =
            Set.of("ADMIN","KITCHEN","MANAGER","SUPERADMIN","WAITER","USER");

    private final Map<String, Set<SseEmitter>> channels = new ConcurrentHashMap<>();
    private final NotificationEventRepository repo;
    private final ObjectMapper mapper;

    public SseEmitter subscribe(String channel, Long lastEventId) {
        channel = norm(channel);
        SseEmitter emitter = new SseEmitter(TIMEOUT);
        channels.computeIfAbsent(channel, k -> ConcurrentHashMap.newKeySet()).add(emitter);
        String finalChannel = channel;
        emitter.onCompletion(() -> remove(finalChannel, emitter));
        emitter.onTimeout(() -> remove(finalChannel, emitter));
        emitter.onError(ex -> remove(finalChannel, emitter));
        try { emitter.send(SseEmitter.event().id("0").name("INIT").data("ok")); } catch (IOException ignored) {}
        replayMissedEvents(lastEventId, emitter, channel);
        return emitter;
    }

    public SseEmitter subscribeMulti(Set<String> chans, Long lastEventId) {
        SseEmitter em = new SseEmitter(TIMEOUT);
        em.onCompletion(() -> chans.forEach(c -> remove(c, em)));
        em.onTimeout(() -> chans.forEach(c -> remove(c, em)));
        em.onError(ex -> chans.forEach(c -> remove(c, em)));
        try { em.send(SseEmitter.event().id("0").name("INIT").data("ok")); } catch (IOException ignored) {}
        for (String ch : chans) {
            String c = norm(ch);
            channels.computeIfAbsent(c, k -> ConcurrentHashMap.newKeySet()).add(em);
            replayMissedEvents(lastEventId, em, c);
        }
        return em;
    }

    private void replayMissedEvents(Long lastEventId, SseEmitter em, String c) {
        if (lastEventId == null) return;
        long cursor = lastEventId;
        while (true) {
            var missed = repo.findTop500ByChannelAndIdGreaterThanOrderByIdAsc(c, cursor);
            if (missed.isEmpty()) break;
            for (var m : missed) {
                try {
                    em.send(SseEmitter.event()
                            .id(Long.toString(m.getId()))
                            .name(m.getEvent())
                            .data(m.getPayload(), MediaType.APPLICATION_JSON));
                } catch (IOException ignored) {}
                cursor = m.getId();
            }
            if (missed.size() < PAGE_SIZE) break;
        }
    }

    public void publish(String channel, String event, Object data) {
        channel = norm(channel);
        if (!channel.startsWith("role:")) {
            String up = channel.toUpperCase();
            if (KNOWN_ROLES.contains(up)) channel = "role:" + up;
        }
        String payload;
        try { payload = mapper.writeValueAsString(data); }
        catch (com.fasterxml.jackson.core.JsonProcessingException e) { payload = String.valueOf(data); }

        var saved = repo.save(pos.pos.Entity.Notification.NotificationEvent.builder()
                .channel(channel).event(event).payload(payload).build());
        deliverLocal(channel, saved.getId(), event, payload);
    }

    public void deliverLocal(String channel, long id, String event, String payload) {
        var set = channels.get(channel);
        if (set == null) return;
        for (SseEmitter e : Set.copyOf(set)) {
            try {
                e.send(SseEmitter.event()
                        .id(Long.toString(id))
                        .name(event)
                        .data(payload, MediaType.APPLICATION_JSON));
            } catch (IOException ex) {
                e.complete();
                set.remove(e);
            }
        }
        if (set.isEmpty()) channels.remove(channel, set);
    }

    @Scheduled(fixedRate = 15000)
    public void heartbeat() {
        channels.forEach((ch, set) -> deliverLocal(ch, 0L, "PING", "\"ok\""));
    }


    @Scheduled(cron = "0 5 * * * *") // every hour at :05
    public void retention() {
        repo.deleteByCreatedAtBefore(Instant.now().minus(Duration.ofHours(72)));
    }

    private void remove(String channel, SseEmitter emitter) {
        var set = channels.get(channel);
        if (set != null) {
            set.remove(emitter);
            if (set.isEmpty()) channels.remove(channel, set);
        }
    }

    private String norm(String ch) { return ch == null ? "" : ch.trim(); }
}
