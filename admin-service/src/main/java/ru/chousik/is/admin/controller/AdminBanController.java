package ru.chousik.is.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chousik.is.admin.client.UserServiceClient;
import ru.chousik.is.admin.dto.ban.BanCreateRequest;
import ru.chousik.is.admin.dto.ban.BanLiftRequest;
import ru.chousik.is.admin.dto.ban.BanResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/bans")
@RequiredArgsConstructor
public class AdminBanController {

    private final UserServiceClient userServiceClient;

    @GetMapping
    public List<BanResponse> getBans(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                     @RequestParam(value = "status", required = false) String status) {
        return userServiceClient.getBans(status, authHeader);
    }

    @GetMapping("/user/{userId}")
    public List<BanResponse> getBansForUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                            @PathVariable UUID userId) {
        return userServiceClient.getBansForUser(userId, authHeader);
    }

    @PostMapping
    public ResponseEntity<BanResponse> createBan(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                                 @Valid @RequestBody BanCreateRequest request) {
        return ResponseEntity.ok(userServiceClient.createBan(request, authHeader));
    }

    @PostMapping("/{banId}/lift")
    public ResponseEntity<BanResponse> liftBan(@PathVariable UUID banId,
                                               @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                               @Valid @RequestBody BanLiftRequest request) {
        return ResponseEntity.ok(userServiceClient.liftBan(banId, request, authHeader));
    }
}
