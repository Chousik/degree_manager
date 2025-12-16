package ru.chousik.is.dto.account;

public record NotificationSettingsDto(
        boolean systemNotifications,
        boolean rentalNotifications,
        boolean messageNotifications,
        boolean paymentNotifications
) {
}
