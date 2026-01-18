package ru.chousik.is.dto.realtime;

public record RealtimeEnvelope(
        String type,
        Object payload
) {
}
