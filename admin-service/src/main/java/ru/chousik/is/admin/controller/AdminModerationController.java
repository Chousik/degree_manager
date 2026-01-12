package ru.chousik.is.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chousik.is.admin.client.UserServiceClient;
import ru.chousik.is.admin.dto.moderation.FlaggedListingDto;
import ru.chousik.is.admin.dto.moderation.FlaggedReviewDto;
import ru.chousik.is.admin.dto.moderation.ModerationResolutionRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/moderation")
@RequiredArgsConstructor
public class AdminModerationController {

    private final UserServiceClient userServiceClient;

    @GetMapping("/listings")
    public List<FlaggedListingDto> getFlaggedListings(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return userServiceClient.getFlaggedListings(authHeader);
    }

    @GetMapping("/reviews")
    public List<FlaggedReviewDto> getFlaggedReviews(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return userServiceClient.getFlaggedReviews(authHeader);
    }

    @PostMapping("/listings/{listingId}/resolve")
    public ResponseEntity<Void> resolveListing(@PathVariable UUID listingId,
                                               @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                               @Valid @RequestBody ModerationResolutionRequest request) {
        userServiceClient.resolveListing(listingId, request, authHeader);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reviews/{reviewId}/resolve")
    public ResponseEntity<Void> resolveReview(@PathVariable UUID reviewId,
                                              @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                              @Valid @RequestBody ModerationResolutionRequest request) {
        userServiceClient.resolveReview(reviewId, request, authHeader);
        return ResponseEntity.noContent().build();
    }
}
