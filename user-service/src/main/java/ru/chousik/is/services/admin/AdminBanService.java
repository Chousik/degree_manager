package ru.chousik.is.services.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.is.dto.admin.ban.BanCreateRequest;
import ru.chousik.is.dto.admin.ban.BanLiftRequest;
import ru.chousik.is.dto.admin.ban.BanResponse;
import ru.chousik.is.entity.BanList;
import ru.chousik.is.entity.User;
import ru.chousik.is.exceptions.BusinessValidationException;
import ru.chousik.is.exceptions.ResourceNotFoundException;
import ru.chousik.is.repository.BanListRepository;
import ru.chousik.is.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminBanService {

    private final BanListRepository banListRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<BanResponse> getBans(String status) {
        List<BanList> bans = status == null || status.isBlank()
                ? banListRepository.findAll()
                : banListRepository.findAllByStatusIgnoreCase(status);
        return bans.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BanResponse> getBansForUser(UUID userId) {
        return banListRepository.findAllByBannedUser_Id(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BanResponse createBan(BanCreateRequest request) {
        User admin = userRepository.findById(request.adminId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin %s not found".formatted(request.adminId())));
        User bannedUser = userRepository.findById(request.bannedUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User %s not found".formatted(request.bannedUserId())));

        String banType = normalizeBanType(request.banType());
        OffsetDateTime banDuration = normalizeDuration(banType, request.banDuration());
        String status = request.status() == null || request.status().isBlank() ? "ACTIVE" : request.status();

        BanList ban = new BanList();
        ban.setAdminUser(admin);
        ban.setBannedUser(bannedUser);
        ban.setBanReason(request.banReason());
        ban.setBanType(banType);
        ban.setBanDuration(banDuration);
        ban.setStatus(status);
        ban.setCreatedAt(OffsetDateTime.now());

        BanList saved = banListRepository.save(ban);
        bannedUser.setLastBan(saved);
        if ("ACTIVE".equalsIgnoreCase(status)) {
            bannedUser.setStatus("BANNED");
        }
        userRepository.save(bannedUser);

        return toResponse(saved);
    }

    @Transactional
    public BanResponse liftBan(UUID banId, BanLiftRequest request) {
        BanList ban = banListRepository.findById(banId)
                .orElseThrow(() -> new ResourceNotFoundException("Ban %s not found".formatted(banId)));
        User admin = userRepository.findById(request.adminId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin %s not found".formatted(request.adminId())));

        ban.setStatus(request.status());
        ban.setAdminUser(admin);
        banListRepository.save(ban);

        if (ban.getBannedUser() != null &&
                ("LIFTED".equalsIgnoreCase(request.status()) || "REVOKED".equalsIgnoreCase(request.status()))) {
            User bannedUser = ban.getBannedUser();
            bannedUser.setStatus("ACTIVE");
            userRepository.save(bannedUser);
        }

        return toResponse(ban);
    }

    private BanResponse toResponse(BanList ban) {
        return new BanResponse(
                ban.getId(),
                ban.getBannedUser() != null ? ban.getBannedUser().getId() : null,
                ban.getAdminUser() != null ? ban.getAdminUser().getId() : null,
                ban.getBanReason(),
                ban.getBanType(),
                ban.getBanDuration(),
                ban.getStatus(),
                ban.getCreatedAt()
        );
    }

    private String normalizeBanType(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessValidationException("Ban type is required");
        }
        String normalized = raw.trim().toUpperCase();
        if (!"TEMP".equals(normalized) && !"PERM".equals(normalized)) {
            throw new BusinessValidationException("Unknown ban type: " + raw);
        }
        return normalized;
    }

    private OffsetDateTime normalizeDuration(String banType, OffsetDateTime banDuration) {
        if ("TEMP".equals(banType)) {
            if (banDuration == null) {
                throw new BusinessValidationException("Ban duration is required for TEMP bans");
            }
            return banDuration;
        }
        return null;
    }
}
