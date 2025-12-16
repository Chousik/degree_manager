package ru.chousik.is.dto.account;

import ru.chousik.is.dto.listing.ListingSummaryDto;

import java.util.List;

public record AccountDashboardResponse(
        AccountProfileDto profile,
        List<ListingSummaryDto> activeListings,
        List<ListingSummaryDto> archivedListings,
        List<ListingSummaryDto> favorites,
        NotificationSettingsDto notificationSettings
) {
}
