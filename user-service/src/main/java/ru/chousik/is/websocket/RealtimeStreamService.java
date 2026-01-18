package ru.chousik.is.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.chousik.is.dto.realtime.RealtimeEnvelope;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RealtimeStreamService {

    private final RealtimeSocketRegistry socketRegistry;
    private final ObjectMapper objectMapper;

    public void send(UUID userId, String type, Object payload) {
        if (userId == null || type == null || type.isBlank() || payload == null) {
            return;
        }
        try {
            RealtimeEnvelope envelope = new RealtimeEnvelope(type, payload);
            String data = objectMapper.writeValueAsString(envelope);
            socketRegistry.send(userId, data);
        } catch (JsonProcessingException ex) {
            log.warn("Failed to serialize realtime payload for user {}", userId, ex);
        }
    }
}
