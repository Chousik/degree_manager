package ru.chousik.is.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import ru.chousik.is.dto.account.*;
import ru.chousik.is.services.UserAccountService;

import java.util.UUID;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class UserAccountController {

    private final UserAccountService accountService;

    @GetMapping("/{userRef}")
    public AccountDashboardResponse getDashboard(@PathVariable String userRef,
                                                 @AuthenticationPrincipal Jwt jwt) {
        return accountService.getDashboard(resolveUserId(userRef, jwt));
    }

    @PutMapping("/{userRef}")
    public AccountProfileDto updateProfile(@PathVariable String userRef,
                                           @AuthenticationPrincipal Jwt jwt,
                                           @Valid @RequestBody ProfileUpdateRequest request) {
        return accountService.updateProfile(resolveUserId(userRef, jwt), request);
    }

    @PutMapping("/{userRef}/notifications")
    public NotificationSettingsDto updateNotifications(@PathVariable String userRef,
                                                       @AuthenticationPrincipal Jwt jwt,
                                                       @Valid @RequestBody NotificationSettingsUpdateRequest request) {
        return accountService.updateNotificationSettings(resolveUserId(userRef, jwt), request);
    }

    @GetMapping("/{userRef}/city")
    public CityResponse getCity(@PathVariable String userRef,
                                @AuthenticationPrincipal Jwt jwt) {
        return new CityResponse(accountService.getCity(resolveUserId(userRef, jwt)));
    }

    @PutMapping("/{userRef}/city")
    public CityResponse updateCity(@PathVariable String userRef,
                                   @AuthenticationPrincipal Jwt jwt,
                                   @Valid @RequestBody CityUpdateRequest request) {
        return new CityResponse(accountService.updateCity(resolveUserId(userRef, jwt), request.city()));
    }

    private UUID resolveUserId(String userRef, Jwt jwt) {
        if ("me".equalsIgnoreCase(userRef)) {
            return currentUserId(jwt);
        }
        try {
            return UUID.fromString(userRef);
        } catch (IllegalArgumentException ignored) {
            return accountService.findUserIdByUsername(userRef);
        }
    }

    private UUID currentUserId(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalStateException("Authentication is required");
        }
        String userIdClaim = jwt.getClaimAsString("user_id");
        if (StringUtils.hasText(userIdClaim)) {
            try {
                return UUID.fromString(userIdClaim);
            } catch (IllegalArgumentException ignored) {
                // fall back to username lookup
            }
        }
        String subject = jwt.getSubject();
        if (StringUtils.hasText(subject)) {
            try {
                return UUID.fromString(subject);
            } catch (IllegalArgumentException ex) {
                return accountService.findUserIdByUsername(subject);
            }
        }
        throw new IllegalStateException("Cannot resolve current user identifier");
    }
}
