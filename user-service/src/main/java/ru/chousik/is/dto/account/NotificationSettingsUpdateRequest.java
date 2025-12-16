package ru.chousik.is.dto.account;

public record NotificationSettingsUpdateRequest(
        Boolean systemNotifications,
        Boolean rentalNotifications,
        Boolean messageNotifications,
        Boolean paymentNotifications
) {
}
