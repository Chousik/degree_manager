package ru.chousik.is.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.chousik.is.dto.account.*;
import ru.chousik.is.services.UserAccountService;

import java.util.UUID;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountService accountService;

    @GetMapping("/{userId}")
    public AccountDashboardResponse getDashboard(@PathVariable UUID userId) {
        return accountService.getDashboard(userId);
    }

    @PutMapping("/{userId}")
    public AccountProfileDto updateProfile(@PathVariable UUID userId,
                                           @Valid @RequestBody ProfileUpdateRequest request) {
        return accountService.updateProfile(userId, request);
    }

    @PutMapping("/{userId}/notifications")
    public NotificationSettingsDto updateNotifications(@PathVariable UUID userId,
                                                       @Valid @RequestBody NotificationSettingsUpdateRequest request) {
        return accountService.updateNotificationSettings(userId, request);
    }

    @GetMapping("/{userId}/city")
    public CityResponse getCity(@PathVariable UUID userId) {
        return new CityResponse(accountService.getCity(userId));
    }

    @PutMapping("/{userId}/city")
    public CityResponse updateCity(@PathVariable UUID userId,
                                   @Valid @RequestBody CityUpdateRequest request) {
        return new CityResponse(accountService.updateCity(userId, request.city()));
    }
}
