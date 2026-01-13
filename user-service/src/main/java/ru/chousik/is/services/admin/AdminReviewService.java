package ru.chousik.is.services.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.is.dto.admin.review.ReviewDetailDto;
import ru.chousik.is.dto.admin.review.ReviewHideRequest;
import ru.chousik.is.entity.ModerationAction;
import ru.chousik.is.entity.Report;
import ru.chousik.is.entity.Review;
import ru.chousik.is.entity.ReviewAuthorRole;
import ru.chousik.is.entity.User;
import ru.chousik.is.exceptions.ResourceNotFoundException;
import ru.chousik.is.repository.ModerationActionRepository;
import ru.chousik.is.repository.ReportRepository;
import ru.chousik.is.repository.ReviewRepository;
import ru.chousik.is.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ModerationActionRepository moderationActionRepository;
    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public ReviewDetailDto getReview(UUID reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review %s not found".formatted(reviewId)));

        return new ReviewDetailDto(
                review.getId(),
                review.getListing() != null ? review.getListing().getId() : null,
                review.getRental() != null ? review.getRental().getId() : null,
                review.getLessor() != null ? review.getLessor().getId() : null,
                review.getLessee() != null ? review.getLessee().getId() : null,
                review.getAuthorRole(),
                review.getRating(),
                review.getText(),
                review.getFlagged(),
                review.getFlagReason(),
                review.getHidden(),
                review.getCreatedAt()
        );
    }

    @Transactional
    public void hideReview(UUID reviewId, ReviewHideRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review %s not found".formatted(reviewId)));
        User admin = userRepository.findById(request.adminId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin %s not found".formatted(request.adminId())));

        review.setHidden(Boolean.TRUE);
        review.setFlagged(Boolean.FALSE);
        review.setFlagReason(null);
        reviewRepository.save(review);

        ModerationAction moderationAction = new ModerationAction();
        moderationAction.setActor(admin);
        moderationAction.setListing(review.getListing());
        moderationAction.setTargetUser(resolveReviewAuthor(review));
        moderationAction.setAction("hide_review");
        moderationAction.setComment(request.comment());
        moderationAction.setCreatedAt(OffsetDateTime.now());
        if (request.reportId() != null) {
            Report report = reportRepository.findById(request.reportId()).orElse(null);
            moderationAction.setReport(report);
        }
        moderationActionRepository.save(moderationAction);
    }

    private User resolveReviewAuthor(Review review) {
        if (review.getAuthorRole() == ReviewAuthorRole.LESSOR) {
            return review.getLessor();
        }
        return review.getLessee();
    }
}
