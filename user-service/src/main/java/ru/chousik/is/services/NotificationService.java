package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.chousik.is.dto.notification.NotificationResponse;
import ru.chousik.is.entity.Notification;
import ru.chousik.is.entity.NotificationPreference;
import ru.chousik.is.entity.User;
import ru.chousik.is.repository.NotificationPreferenceRepository;
import ru.chousik.is.repository.NotificationRepository;
import ru.chousik.is.websocket.RealtimeStreamService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;
    private final RealtimeStreamService realtimeStreamService;

    public List<NotificationResponse> getUserNotifications(UUID userId) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void createRentalNotification(User user, String body) {
        createNotification(user, "RENTAL", body, NotificationPreference::getRentalNotifications);
    }

    public void createMessageNotification(User user, String body) {
        createNotification(user, "MESSAGE", body, NotificationPreference::getMessageNotifications);
    }

    public void createPaymentNotification(User user, String body) {
        createNotification(user, "PAYMENT", body, NotificationPreference::getPaymentNotifications);
    }

    public void createSystemNotification(User user, String body) {
        createNotification(user, "SYSTEM", body, NotificationPreference::getSystemNotifications);
    }

    private void createNotification(User user,
                                    String type,
                                    String body,
                                    java.util.function.Function<NotificationPreference, Boolean> preferenceSelector) {
        if (user == null || body == null || body.isBlank()) {
            return;
        }
        NotificationPreference preference = notificationPreferenceRepository.findByUser_Id(user.getId()).orElse(null);
        if (preference != null && Boolean.FALSE.equals(preferenceSelector.apply(preference))) {
            return;
        }
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setBody(body);
        notification.setIsRead(Boolean.FALSE);
        notification.setCreatedAt(OffsetDateTime.now());
        Notification saved = notificationRepository.save(notification);
        realtimeStreamService.send(user.getId(), "notification", mapToResponse(saved));
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getBody(),
                notification.getIsRead(),
                notification.getCreatedAt()
        );
    }
}
