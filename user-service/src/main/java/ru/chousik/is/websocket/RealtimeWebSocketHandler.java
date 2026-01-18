package ru.chousik.is.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RealtimeWebSocketHandler extends TextWebSocketHandler {

    private final RealtimeSocketRegistry socketRegistry;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID userId = resolveUserId(session.getUri());
        if (userId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        socketRegistry.register(userId, session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        socketRegistry.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        socketRegistry.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // Clients are not expected to send messages. Ignore.
    }

    private UUID resolveUserId(URI uri) {
        if (uri == null || !StringUtils.hasText(uri.getQuery())) {
            return null;
        }
        String[] params = uri.getQuery().split("&");
        for (String param : params) {
            String[] parts = param.split("=", 2);
            if (parts.length == 2 && "userId".equals(parts[0]) && StringUtils.hasText(parts[1])) {
                try {
                    return UUID.fromString(parts[1]);
                } catch (IllegalArgumentException ex) {
                    return null;
                }
            }
        }
        return null;
    }
}
