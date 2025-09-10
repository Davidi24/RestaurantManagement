package pos.pos.Service.Notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import pos.pos.Repository.Notification.NotificationEventRepository;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
@Service
public class SseHub {
    private static final long TIMEOUT = Duration.ofMinutes(30).toMillis();

    private final Map<String, Set<SseEmitter>> channels = new ConcurrentHashMap<>();
    private final NotificationEventRepository repo;
    private final com.fasterxml.jackson.databind.ObjectMapper mapper;

    private String norm(String ch) { return ch == null ? "" : ch.trim(); }

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
        if (lastEventId != null) {
            var missed = repo.findTop500ByChannelAndIdGreaterThanOrderByIdAsc(c, lastEventId);
            for (var m : missed) {
                try {
                    em.send(SseEmitter.event()
                            .id(Long.toString(m.getId()))
                            .name(m.getEvent())
                            .data(m.getPayload()));
                } catch (IOException ignored) {}
            }
        }
    }

    public void publish(String channel, String event, Object data) {
        channel = norm(channel);
        if (!channel.startsWith("role:")) {
            if (channel.equalsIgnoreCase("ADMIN") ||
                    channel.equalsIgnoreCase("KITCHEN") ||
                    channel.equalsIgnoreCase("MANAGER")) {
                channel = "role:" + channel.toUpperCase();
            }
        }
        String payload;
        try { payload = mapper.writeValueAsString(data); }
        catch (com.fasterxml.jackson.core.JsonProcessingException e) { payload = String.valueOf(data); }

        var saved = repo.save(pos.pos.Entity.Notification.NotificationEvent.builder()
                .channel(channel).event(event).payload(payload).build());
        long id = saved.getId();

        deliverLocal(channel, id, event, payload);
    }

    public void deliverLocal(String channel, long id, String event, String payload) {
        var set = channels.get(channel);
        if (set == null) return;
        for (SseEmitter e : set) {
            try { e.send(SseEmitter.event().id(Long.toString(id)).name(event).data(payload)); }
            catch (IOException ex) { e.complete(); set.remove(e); }
        }
        if (set.isEmpty()) channels.remove(channel, set);
    }

    private void remove(String channel, SseEmitter emitter) {
        var set = channels.get(channel);
        if (set != null) {
            set.remove(emitter);
            if (set.isEmpty()) channels.remove(channel, set);
        }
    }
}
