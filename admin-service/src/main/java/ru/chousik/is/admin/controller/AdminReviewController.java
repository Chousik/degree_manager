package ru.chousik.is.admin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import ru.chousik.is.admin.client.UserServiceClient;
import ru.chousik.is.admin.dto.review.ReviewDetailDto;
import ru.chousik.is.admin.dto.review.ReviewHideRequest;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final UserServiceClient userServiceClient;

    @GetMapping("/{reviewId}")
    public ReviewDetailDto getReview(@PathVariable UUID reviewId,
                                     @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return userServiceClient.getReviewDetail(reviewId, authHeader);
    }

    @PostMapping("/{reviewId}/hide")
    public void hideReview(@PathVariable UUID reviewId,
                           @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                           @RequestBody ReviewHideRequest request) {
        userServiceClient.hideReview(reviewId, request, authHeader);
    }
}
