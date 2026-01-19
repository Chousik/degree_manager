package ru.chousik.is.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.chousik.is.dto.notification.NotificationResponse;
import ru.chousik.is.entity.Notification;
import ru.chousik.is.entity.NotificationPreference;
import ru.chousik.is.entity.User;
import ru.chousik.is.repository.NotificationPreferenceRepository;
import ru.chousik.is.repository.NotificationRepository;
import ru.chousik.is.websocket.RealtimeStreamService;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @Mock
    private RealtimeStreamService realtimeStreamService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void getUserNotifications_mapsToResponses() {
        UUID userId = UUID.randomUUID();
        Notification notification = new Notification();
        notification.setId(UUID.randomUUID());
        notification.setType("SYSTEM");
        notification.setBody("Hello");
        notification.setIsRead(false);
        notification.setCreatedAt(OffsetDateTime.now());
        when(notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(notification));

        List<NotificationResponse> responses = notificationService.getUserNotifications(userId);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).type()).isEqualTo("SYSTEM");
        assertThat(responses.get(0).body()).isEqualTo("Hello");
    }

    @Test
    void createMessageNotification_respectsDisabledPreference() {
        User user = new User();
        user.setId(UUID.randomUUID());
        NotificationPreference preference = new NotificationPreference();
        preference.setMessageNotifications(false);
        when(notificationPreferenceRepository.findByUser_Id(user.getId()))
                .thenReturn(Optional.of(preference));

        notificationService.createMessageNotification(user, "New message");

        verify(notificationRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(realtimeStreamService, never()).send(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createSystemNotification_sendsRealtimeUpdate() {
        User user = new User();
        user.setId(UUID.randomUUID());
        when(notificationPreferenceRepository.findByUser_Id(user.getId()))
                .thenReturn(Optional.empty());
        when(notificationRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            Notification saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        notificationService.createSystemNotification(user, "System update");

        ArgumentCaptor<NotificationResponse> captor = ArgumentCaptor.forClass(NotificationResponse.class);
        verify(realtimeStreamService).send(org.mockito.ArgumentMatchers.eq(user.getId()),
                org.mockito.ArgumentMatchers.eq("notification"),
                captor.capture());
        assertThat(captor.getValue().type()).isEqualTo("SYSTEM");
        assertThat(captor.getValue().body()).isEqualTo("System update");
    }
}
