package ru.chousik.is.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.chousik.is.dto.account.NotificationSettingsDto;
import ru.chousik.is.dto.account.NotificationSettingsUpdateRequest;
import ru.chousik.is.entity.NotificationPreference;
import ru.chousik.is.entity.User;
import ru.chousik.is.repository.ListingRepository;
import ru.chousik.is.repository.NotificationPreferenceRepository;
import ru.chousik.is.repository.UserRepository;
import ru.chousik.is.services.mappers.ListingSummaryMapper;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private ListingSummaryMapper listingSummaryMapper;

    @Mock
    private FavoriteService favoriteService;

    @Mock
    private NotificationPreferenceRepository notificationPreferenceRepository;

    @InjectMocks
    private UserAccountService userAccountService;

    @Test
    void updateCity_defaultsWhenBlank() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        String result = userAccountService.updateCity(userId, "  ");

        assertThat(result).isEqualTo("Москва");
        verify(userRepository).save(user);
    }

    @Test
    void updateNotificationSettings_updatesFlags() {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        NotificationPreference preference = new NotificationPreference();
        preference.setUser(user);
        preference.setSystemNotifications(false);
        preference.setRentalNotifications(true);
        preference.setMessageNotifications(false);
        preference.setPaymentNotifications(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(notificationPreferenceRepository.findByUser_Id(userId)).thenReturn(Optional.of(preference));

        NotificationSettingsUpdateRequest request = new NotificationSettingsUpdateRequest(true, false, true, false);
        NotificationSettingsDto result = userAccountService.updateNotificationSettings(userId, request);

        assertThat(result.systemNotifications()).isTrue();
        assertThat(result.rentalNotifications()).isFalse();
        assertThat(result.messageNotifications()).isTrue();
        assertThat(result.paymentNotifications()).isFalse();
        verify(notificationPreferenceRepository).save(preference);
    }
}
