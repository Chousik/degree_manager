package ru.chousik.is.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.chousik.is.dto.review.ReviewSummaryResponse;
import ru.chousik.is.services.ReviewService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/lessor")
    public List<ReviewSummaryResponse> getLessorReviews(@RequestParam UUID lessorId) {
        return reviewService.getLessorReviews(lessorId);
    }
}
