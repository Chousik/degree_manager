package ru.chousik.web.authservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.chousik.web.authservice.dto.TwoFactorCodeRequest;
import ru.chousik.web.authservice.dto.TwoFactorSetupResponse;
import ru.chousik.web.authservice.dto.TwoFactorStatusResponse;
import ru.chousik.web.authservice.services.TwoFactorService;

@RestController
@RequestMapping("/users/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    @GetMapping
    public TwoFactorStatusResponse status(@AuthenticationPrincipal Jwt jwt) {
        return twoFactorService.getStatus(resolveUsername(jwt));
    }

    @PostMapping("/setup")
    public TwoFactorSetupResponse setup(@AuthenticationPrincipal Jwt jwt) {
        return twoFactorService.initiateSetup(resolveUsername(jwt));
    }

    @PostMapping("/enable")
    public TwoFactorStatusResponse enable(@AuthenticationPrincipal Jwt jwt,
                                          @Valid @RequestBody TwoFactorCodeRequest request) {
        twoFactorService.enable(resolveUsername(jwt), request.code());
        return new TwoFactorStatusResponse(true);
    }

    @PostMapping("/disable")
    public TwoFactorStatusResponse disable(@AuthenticationPrincipal Jwt jwt,
                                           @Valid @RequestBody TwoFactorCodeRequest request) {
        twoFactorService.disable(resolveUsername(jwt), request.code());
        return new TwoFactorStatusResponse(false);
    }

    private String resolveUsername(Jwt jwt) {
        if (jwt == null) {
            throw new IllegalStateException("Authentication required");
        }
        String username = jwt.getClaimAsString("preferred_username");
        if (!StringUtils.hasText(username)) {
            username = jwt.getSubject();
        }
        if (!StringUtils.hasText(username)) {
            username = jwt.getClaimAsString("user_name");
        }
        if (!StringUtils.hasText(username)) {
            throw new IllegalStateException("Cannot resolve username from token");
        }
        return username;
    }
}
