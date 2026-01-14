package ru.chousik.web.authservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
@RequestMapping("/api/users/2fa")
@RequiredArgsConstructor
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    @GetMapping
    public TwoFactorStatusResponse status(@AuthenticationPrincipal UserDetails userDetails) {
        return twoFactorService.getStatus(userDetails.getUsername());
    }

    @PostMapping("/setup")
    public TwoFactorSetupResponse setup(@AuthenticationPrincipal UserDetails userDetails) {
        return twoFactorService.initiateSetup(userDetails.getUsername());
    }

    @PostMapping("/enable")
    public TwoFactorStatusResponse enable(@AuthenticationPrincipal UserDetails userDetails,
                                          @Valid @RequestBody TwoFactorCodeRequest request) {
        twoFactorService.enable(userDetails.getUsername(), request.code());
        return new TwoFactorStatusResponse(true);
    }

    @PostMapping("/disable")
    public TwoFactorStatusResponse disable(@AuthenticationPrincipal UserDetails userDetails,
                                           @Valid @RequestBody TwoFactorCodeRequest request) {
        twoFactorService.disable(userDetails.getUsername(), request.code());
        return new TwoFactorStatusResponse(false);
    }
}
