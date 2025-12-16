package ru.chousik.is.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chousik.is.dto.moderation.FlagRequest;
import ru.chousik.is.dto.moderation.FlaggedListingDto;
import ru.chousik.is.dto.moderation.FlaggedReviewDto;
import ru.chousik.is.dto.moderation.ModerationResolutionRequest;
import ru.chousik.is.services.ModerationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/moderation")
@RequiredArgsConstructor
public class ModerationController {

    private final ModerationService moderationService;

    @PostMapping("/listings/{listingId}/flag")
    public ResponseEntity<Void> flagListing(@PathVariable UUID listingId,
                                            @Valid @RequestBody FlagRequest request) {
        moderationService.flagListing(listingId, request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reviews/{reviewId}/flag")
    public ResponseEntity<Void> flagReview(@PathVariable UUID reviewId,
                                           @Valid @RequestBody FlagRequest request) {
        moderationService.flagReview(reviewId, request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/listings/{listingId}/resolve")
    public ResponseEntity<Void> resolveListing(@PathVariable UUID listingId,
                                               @Valid @RequestBody ModerationResolutionRequest request) {
        moderationService.resolveListingFlag(listingId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reviews/{reviewId}/resolve")
    public ResponseEntity<Void> resolveReview(@PathVariable UUID reviewId,
                                              @Valid @RequestBody ModerationResolutionRequest request) {
        moderationService.resolveReviewFlag(reviewId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/listings")
    public List<FlaggedListingDto> getFlaggedListings() {
        return moderationService.getFlaggedListings();
    }

    @GetMapping("/reviews")
    public List<FlaggedReviewDto> getFlaggedReviews() {
        return moderationService.getFlaggedReviews();
    }
}
