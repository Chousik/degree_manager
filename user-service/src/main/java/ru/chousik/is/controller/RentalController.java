package ru.chousik.is.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.chousik.is.dto.message.MessageDto;
import ru.chousik.is.dto.message.MessageRequest;
import ru.chousik.is.dto.rental.RentalActionRequest;
import ru.chousik.is.dto.rental.RentalCreateRequest;
import ru.chousik.is.dto.rental.RentalResponse;
import ru.chousik.is.dto.review.ReviewRequest;
import ru.chousik.is.dto.review.ReviewResponse;
import ru.chousik.is.services.MessagingService;
import ru.chousik.is.services.RentalService;
import ru.chousik.is.services.ReviewService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rentals")
@Validated
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;
    private final MessagingService messagingService;
    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<RentalResponse> createRental(@Valid @RequestBody RentalCreateRequest request) {
        RentalResponse response = rentalService.createRental(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{rentalId}/confirm")
    public RentalResponse confirmRental(@PathVariable UUID rentalId,
                                        @Valid @RequestBody RentalActionRequest request) {
        return rentalService.confirmRental(rentalId, request.actorId());
    }

    @PostMapping("/{rentalId}/cancel")
    public RentalResponse cancelRental(@PathVariable UUID rentalId,
                                       @Valid @RequestBody RentalActionRequest request) {
        return rentalService.cancelRental(rentalId, request.actorId());
    }

    @PostMapping("/{rentalId}/complete")
    public RentalResponse completeRental(@PathVariable UUID rentalId,
                                         @Valid @RequestBody RentalActionRequest request) {
        return rentalService.completeRental(rentalId, request.actorId());
    }

    @GetMapping("/{rentalId}/messages")
    public List<MessageDto> getMessages(@PathVariable UUID rentalId,
                                        @RequestParam UUID userId) {
        return messagingService.getMessages(rentalId, userId);
    }

    @PostMapping("/{rentalId}/messages")
    public MessageDto sendMessage(@PathVariable UUID rentalId,
                                  @Valid @RequestBody MessageRequest request) {
        return messagingService.sendMessage(rentalId, request);
    }

    @PostMapping("/{rentalId}/reviews")
    public ReviewResponse leaveReview(@PathVariable UUID rentalId,
                                      @Valid @RequestBody ReviewRequest request) {
        return reviewService.leaveReview(rentalId, request);
    }
}
