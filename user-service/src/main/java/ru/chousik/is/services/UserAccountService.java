package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.chousik.is.dto.account.*;
import ru.chousik.is.entity.Listing;
import ru.chousik.is.entity.ListingStatus;
import ru.chousik.is.entity.NotificationPreference;
import ru.chousik.is.entity.User;
import ru.chousik.is.dto.listing.ListingSummaryDto;
import ru.chousik.is.exceptions.ResourceNotFoundException;
import ru.chousik.is.repository.ListingRepository;
import ru.chousik.is.repository.NotificationPreferenceRepository;
import ru.chousik.is.repository.UserRepository;
import ru.chousik.is.services.mappers.ListingSummaryMapper;

import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private static final EnumSet<ListingStatus> ACTIVE_STATUSES = EnumSet.of(ListingStatus.AVAILABLE, ListingStatus.RENTED);

    private final UserRepository userRepository;
    private final ListingRepository listingRepository;
    private final ListingSummaryMapper listingSummaryMapper;
    private final FavoriteService favoriteService;
    private final NotificationPreferenceRepository notificationPreferenceRepository;

    @Transactional(readOnly = true)
    public AccountDashboardResponse getDashboard(UUID userId) {
        User user = getUser(userId);
        List<Listing> listings = listingRepository.findAllByOwner_Id(userId);
        List<ListingSummaryDto> active = listingSummaryMapper.toSummaryList(
                listings.stream()
                        .filter(listing -> listing.getStatus() == null || ACTIVE_STATUSES.contains(listing.getStatus()))
                        .toList()
        );
        List<ListingSummaryDto> archived = listingSummaryMapper.toSummaryList(
                listings.stream()
                        .filter(listing -> listing.getStatus() == ListingStatus.ARCHIVED)
                        .toList()
        );
        List<ListingSummaryDto> favorites = favoriteService.getFavorites(userId);
        NotificationPreference preference = notificationPreferenceRepository.findByUser_Id(userId).orElse(null);
        NotificationSettingsDto notificationSettings = mapSettings(preference);
        return new AccountDashboardResponse(
                toProfileDto(user),
                active,
                archived,
                favorites,
                notificationSettings
        );
    }

    @Transactional
    public AccountProfileDto updateProfile(UUID userId, ProfileUpdateRequest request) {
        User user = getUser(userId);
        user.setName(request.name());
        user.setSurname(request.surname());
        user.setLastName(request.lastName());
        user.setPhone(request.phone());
        if (StringUtils.hasText(request.city())) {
            user.setCity(normalizeCity(request.city()));
        }
        userRepository.save(user);
        return toProfileDto(user);
    }

    @Transactional(readOnly = true)
    public String getCity(UUID userId) {
        return getUser(userId).getCity();
    }

    @Transactional
    public String updateCity(UUID userId, String city) {
        User user = getUser(userId);
        user.setCity(normalizeCity(city));
        userRepository.save(user);
        return user.getCity();
    }

    @Transactional
    public NotificationSettingsDto updateNotificationSettings(UUID userId, NotificationSettingsUpdateRequest request) {
        User user = getUser(userId);
        NotificationPreference preference = getOrCreatePreference(user);
        if (request.systemNotifications() != null) {
            preference.setSystemNotifications(request.systemNotifications());
        }
        if (request.rentalNotifications() != null) {
            preference.setRentalNotifications(request.rentalNotifications());
        }
        if (request.messageNotifications() != null) {
            preference.setMessageNotifications(request.messageNotifications());
        }
        if (request.paymentNotifications() != null) {
            preference.setPaymentNotifications(request.paymentNotifications());
        }
        preference.setUpdatedAt(OffsetDateTime.now());
        notificationPreferenceRepository.save(preference);
        return mapSettings(preference);
    }

    private AccountProfileDto toProfileDto(User user) {
        return new AccountProfileDto(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getName(),
                user.getSurname(),
                user.getLastName(),
                user.getPhone(),
                user.getCity(),
                user.getRating(),
                user.getStatus(),
                user.getCreatedAt()
        );
    }

    private User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User %s not found".formatted(userId)));
    }

    private NotificationPreference getOrCreatePreference(User user) {
        NotificationPreference preference = new NotificationPreference();
        return notificationPreferenceRepository.findByUser_Id(user.getId())
                .orElseGet(() -> {
                    preference.setUser(user);
                    preference.setSystemNotifications(Boolean.TRUE);
                    preference.setRentalNotifications(Boolean.TRUE);
                    preference.setMessageNotifications(Boolean.TRUE);
                    preference.setPaymentNotifications(Boolean.TRUE);
                    return preference;
                });
    }

    private NotificationSettingsDto mapSettings(NotificationPreference preference) {
        return new NotificationSettingsDto(
                preference == null || Boolean.TRUE.equals(preference.getSystemNotifications()),
                preference == null || Boolean.TRUE.equals(preference.getRentalNotifications()),
                preference == null || Boolean.TRUE.equals(preference.getMessageNotifications()),
                preference == null || Boolean.TRUE.equals(preference.getPaymentNotifications())
        );
    }

    @Transactional(readOnly = true)
    public UUID findUserIdByUsername(String username) {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResourceNotFoundException("User %s not found".formatted(username)));
        return user.getId();
    }

    private String normalizeCity(String value) {
        if (!StringUtils.hasText(value)) {
            return "Москва";
        }
        return value.trim();
    }
}
