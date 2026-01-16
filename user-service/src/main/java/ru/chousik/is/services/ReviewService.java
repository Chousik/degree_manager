package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.is.dto.review.ReviewRequest;
import ru.chousik.is.dto.review.ReviewResponse;
import ru.chousik.is.dto.review.ReviewSummaryResponse;
import ru.chousik.is.entity.Rental;
import ru.chousik.is.entity.RentalStatus;
import ru.chousik.is.entity.Review;
import ru.chousik.is.entity.ReviewAuthorRole;
import ru.chousik.is.exceptions.BusinessValidationException;
import ru.chousik.is.repository.ReviewRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final RentalService rentalService;
    private final ReviewRepository reviewRepository;
    private final ReputationService reputationService;

    @Transactional
    public ReviewResponse leaveReview(UUID rentalId, ReviewRequest request) {
        Rental rental = rentalService.findRental(rentalId);
        if (rental.getStatus() != RentalStatus.COMPLETED && rental.getStatus() != RentalStatus.CANCELLED) {
            throw new BusinessValidationException("Reviews available only after rental completion or cancellation");
        }
        ReviewAuthorRole role = resolveAuthorRole(rental, request.authorId());
        if (reviewRepository.existsByRental_IdAndAuthorRole(rentalId, role)) {
            throw new BusinessValidationException("Review from this side already exists");
        }
        Review review = new Review();
        review.setRental(rental);
        review.setListing(rental.getListing());
        review.setLessor(rental.getLessor());
        review.setLessee(rental.getLessee());
        review.setAuthorRole(role);
        review.setRating(request.rating());
        review.setText(request.text());
        review.setCreatedAt(OffsetDateTime.now());
        Review saved = reviewRepository.save(review);
        updateReputation(saved);
        return map(saved);
    }

    @Transactional(readOnly = true)
    public List<ReviewSummaryResponse> getLessorReviews(UUID lessorId) {
        if (lessorId == null) {
            return List.of();
        }
        return reviewRepository.findAllByLessor_IdAndHiddenFalseAndAuthorRoleOrderByCreatedAtDesc(
                        lessorId,
                        ReviewAuthorRole.LESSEE
                )
                .stream()
                .map(this::mapSummary)
                .toList();
    }

    private ReviewAuthorRole resolveAuthorRole(Rental rental, UUID authorId) {
        if (rental.getLessor() != null && Objects.equals(rental.getLessor().getId(), authorId)) {
            return ReviewAuthorRole.LESSOR;
        }
        if (rental.getLessee() != null && Objects.equals(rental.getLessee().getId(), authorId)) {
            return ReviewAuthorRole.LESSEE;
        }
        throw new BusinessValidationException("User is not a participant of rental");
    }

    private ReviewResponse map(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getRental() != null ? review.getRental().getId() : null,
                review.getAuthorRole(),
                review.getRating(),
                review.getText(),
                review.getCreatedAt()
        );
    }

    private ReviewSummaryResponse mapSummary(Review review) {
        return new ReviewSummaryResponse(
                review.getId(),
                review.getRental() != null ? review.getRental().getId() : null,
                review.getListing() != null ? review.getListing().getId() : null,
                review.getListing() != null ? review.getListing().getTitle() : null,
                review.getAuthorRole(),
                review.getRating(),
                review.getText(),
                review.getRental() != null ? review.getRental().getStatus() : null,
                review.getCreatedAt()
        );
    }

    private void updateReputation(Review review) {
        if (review.getAuthorRole() == ReviewAuthorRole.LESSEE && review.getLessor() != null) {
            reputationService.updateReputationForLessor(review.getLessor().getId());
        } else if (review.getAuthorRole() == ReviewAuthorRole.LESSOR && review.getLessee() != null) {
            reputationService.updateReputationForLessee(review.getLessee().getId());
        }
    }
}
