package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.chousik.is.dto.notification.NotificationResponse;
import ru.chousik.is.entity.Notification;
import ru.chousik.is.entity.NotificationPreference;
import ru.chousik.is.entity.User;
import ru.chousik.is.repository.NotificationPreferenceRepository;
import ru.chousik.is.repository.NotificationRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository notificationPreferenceRepository;

    public List<NotificationResponse> getUserNotifications(UUID userId) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public void createRentalNotification(User user, String body) {
        if (user == null || body == null || body.isBlank()) {
            return;
        }
        NotificationPreference preference = notificationPreferenceRepository.findByUser_Id(user.getId()).orElse(null);
        if (preference != null && Boolean.FALSE.equals(preference.getRentalNotifications())) {
            return;
        }
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType("RENTAL");
        notification.setBody(body);
        notification.setIsRead(Boolean.FALSE);
        notification.setCreatedAt(OffsetDateTime.now());
        notificationRepository.save(notification);
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
