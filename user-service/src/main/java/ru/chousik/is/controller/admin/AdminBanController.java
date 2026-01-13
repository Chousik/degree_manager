package ru.chousik.is.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chousik.is.dto.admin.ban.BanCreateRequest;
import ru.chousik.is.dto.admin.ban.BanLiftRequest;
import ru.chousik.is.dto.admin.ban.BanResponse;
import ru.chousik.is.services.admin.AdminBanService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/bans")
@RequiredArgsConstructor
public class AdminBanController {

    private final AdminBanService adminBanService;

    @GetMapping
    public List<BanResponse> getBans(@RequestParam(value = "status", required = false) String status) {
        return adminBanService.getBans(status);
    }

    @GetMapping("/user/{userId}")
    public List<BanResponse> getBansForUser(@PathVariable UUID userId) {
        return adminBanService.getBansForUser(userId);
    }

    @PostMapping
    public ResponseEntity<BanResponse> createBan(@Valid @RequestBody BanCreateRequest request) {
        return ResponseEntity.ok(adminBanService.createBan(request));
    }

    @PostMapping("/{banId}/lift")
    public ResponseEntity<BanResponse> liftBan(@PathVariable UUID banId,
                                               @Valid @RequestBody BanLiftRequest request) {
        return ResponseEntity.ok(adminBanService.liftBan(banId, request));
    }
}
