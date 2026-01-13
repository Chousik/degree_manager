package ru.chousik.is.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chousik.is.dto.admin.review.ReviewDetailDto;
import ru.chousik.is.dto.admin.review.ReviewHideRequest;
import jakarta.validation.Valid;
import ru.chousik.is.services.admin.AdminReviewService;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final AdminReviewService adminReviewService;

    @GetMapping("/{reviewId}")
    public ReviewDetailDto getReview(@PathVariable UUID reviewId) {
        return adminReviewService.getReview(reviewId);
    }

    @PostMapping("/{reviewId}/hide")
    public ResponseEntity<Void> hideReview(@PathVariable UUID reviewId,
                                           @Valid @RequestBody ReviewHideRequest request) {
        adminReviewService.hideReview(reviewId, request);
        return ResponseEntity.ok().build();
    }
}
