package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.is.dto.moderation.FlagRequest;
import ru.chousik.is.dto.moderation.FlaggedListingDto;
import ru.chousik.is.dto.moderation.FlaggedReviewDto;
import ru.chousik.is.dto.moderation.ModerationResolutionRequest;
import ru.chousik.is.entity.Listing;
import ru.chousik.is.entity.ModerationAction;
import ru.chousik.is.entity.Report;
import ru.chousik.is.entity.Rental;
import ru.chousik.is.entity.Review;
import ru.chousik.is.entity.ReviewAuthorRole;
import ru.chousik.is.entity.User;
import ru.chousik.is.repository.ListingRepository;
import ru.chousik.is.repository.RentalRepository;
import ru.chousik.is.repository.ModerationActionRepository;
import ru.chousik.is.repository.ReportRepository;
import ru.chousik.is.repository.ReviewRepository;
import ru.chousik.is.repository.UserRepository;
import ru.chousik.is.exceptions.ResourceNotFoundException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModerationService {

    private final ListingRepository listingRepository;
    private final ReviewRepository reviewRepository;
    private final RentalRepository rentalRepository;
    private final ModerationActionRepository moderationActionRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final NotificationService notificationService;

    @Transactional
    public void flagListing(UUID listingId, FlagRequest request) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing %s not found".formatted(listingId)));
        listing.setFlagged(Boolean.TRUE);
        listing.setFlagReason(request.reason());
        listingRepository.save(listing);
        createReportForListing(listing, request.reporterId(), request.reason());
        saveModerationAction(listing.getOwner(), request.reporterId(), listing, request.reason(), "flag_listing");
    }

    @Transactional
    public void flagReview(UUID reviewId, FlagRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review %s not found".formatted(reviewId)));
        review.setFlagged(Boolean.TRUE);
        review.setFlagReason(request.reason());
        reviewRepository.save(review);
        createReportForReview(review, request.reporterId(), request.reason());
        saveModerationAction(review.getLessor(), request.reporterId(), review.getListing(), request.reason(), "flag_review");
    }

    @Transactional
    public void flagRental(UUID rentalId, FlagRequest request) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new ResourceNotFoundException("Rental %s not found".formatted(rentalId)));
        createReportForRental(rental, request.reporterId(), request.reason());
        saveModerationAction(rental.getLessor(), request.reporterId(), rental.getListing(), request.reason(), "flag_rental");
    }

    @Transactional
    public void resolveListingFlag(UUID listingId, ModerationResolutionRequest request) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing %s not found".formatted(listingId)));
        listing.setFlagged(Boolean.FALSE);
        listing.setFlagReason(null);
        listingRepository.save(listing);
        resolveReportsForListing(listingId, request);
        saveModerationAction(listing.getOwner(), request.adminId(), listing, request.comment(), request.action());
        notifyListingResolution(listing, request);
    }

    @Transactional
    public void resolveReviewFlag(UUID reviewId, ModerationResolutionRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review %s not found".formatted(reviewId)));
        review.setFlagged(Boolean.FALSE);
        review.setFlagReason(null);
        reviewRepository.save(review);
        resolveReportsForReview(reviewId, request);
        saveModerationAction(review.getLessee(), request.adminId(), review.getListing(), request.comment(), request.action());
        notifyReviewResolution(review, request);
    }

    @Transactional(readOnly = true)
    public List<FlaggedListingDto> getFlaggedListings() {
        return listingRepository.findAllByFlaggedTrue().stream()
                .map(listing -> new FlaggedListingDto(
                        listing.getId(),
                        listing.getTitle(),
                        listing.getPricePerHour(),
                        listing.getFlagReason(),
                        listing.getCreatedAt()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FlaggedReviewDto> getFlaggedReviews() {
        return reviewRepository.findAllByFlaggedTrue().stream()
                .map(review -> new FlaggedReviewDto(
                        review.getId(),
                        review.getRental() != null ? review.getRental().getId() : null,
                        review.getAuthorRole(),
                        review.getRating(),
                        review.getText(),
                        review.getFlagReason(),
                        review.getCreatedAt()
                ))
                .toList();
    }

    private void saveModerationAction(User targetUser, UUID actorId, Listing listing, String comment, String action) {
        if (actorId == null) {
            return;
        }
        User actor = userRepository.findById(actorId).orElse(null);
        ModerationAction moderationAction = new ModerationAction();
        moderationAction.setActor(actor);
        moderationAction.setListing(listing);
        moderationAction.setTargetUser(targetUser);
        moderationAction.setAction(action);
        moderationAction.setComment(comment);
        moderationAction.setCreatedAt(OffsetDateTime.now());
        moderationActionRepository.save(moderationAction);
    }

    private void createReportForListing(Listing listing, UUID reporterId, String reason) {
        Report report = new Report();
        report.setStatus("OPEN");
        report.setTargetType("LISTING");
        report.setTargetId(listing.getId());
        report.setReasonBody(reason);
        report.setCreatedAt(OffsetDateTime.now());
        if (reporterId != null) {
            userRepository.findById(reporterId).ifPresent(report::setReporter);
        }
        reportRepository.save(report);
    }

    private void createReportForReview(Review review, UUID reporterId, String reason) {
        Report report = new Report();
        report.setStatus("OPEN");
        report.setTargetType("REVIEW");
        report.setTargetId(review.getId());
        report.setReasonBody(reason);
        report.setCreatedAt(OffsetDateTime.now());
        if (reporterId != null) {
            userRepository.findById(reporterId).ifPresent(report::setReporter);
        }
        reportRepository.save(report);
    }

    private void createReportForRental(Rental rental, UUID reporterId, String reason) {
        Report report = new Report();
        report.setStatus("OPEN");
        report.setTargetType("RENTAL");
        report.setTargetId(rental.getId());
        report.setReasonBody(reason);
        report.setCreatedAt(OffsetDateTime.now());
        if (reporterId != null) {
            userRepository.findById(reporterId).ifPresent(report::setReporter);
        }
        reportRepository.save(report);
    }

    private void resolveReportsForListing(UUID listingId, ModerationResolutionRequest request) {
        List<Report> reports = reportRepository.findAllByTargetTypeIgnoreCaseAndTargetIdAndStatusIgnoreCase(
                "LISTING",
                listingId,
                "OPEN"
        );
        if (reports.isEmpty()) {
            return;
        }
        User admin = userRepository.findById(request.adminId()).orElse(null);
        for (Report report : reports) {
            report.setStatus("RESOLVED");
            report.setResolvedBy(admin);
        }
        reportRepository.saveAll(reports);
    }

    private void resolveReportsForReview(UUID reviewId, ModerationResolutionRequest request) {
        List<Report> reports = reportRepository.findAllByTargetTypeIgnoreCaseAndTargetIdAndStatusIgnoreCase(
                "REVIEW",
                reviewId,
                "OPEN"
        );
        if (reports.isEmpty()) {
            return;
        }
        User admin = userRepository.findById(request.adminId()).orElse(null);
        for (Report report : reports) {
            report.setStatus("RESOLVED");
            report.setResolvedBy(admin);
        }
        reportRepository.saveAll(reports);
    }

    private void notifyListingResolution(Listing listing, ModerationResolutionRequest request) {
        if (listing == null || listing.getOwner() == null) {
            return;
        }
        String title = listing.getTitle() != null ? listing.getTitle() : "ваше объявление";
        String message = request.comment() != null && !request.comment().isBlank()
                ? String.format("Модерация по \"%s\": %s. Комментарий: %s.", title, request.action(), request.comment())
                : String.format("Модерация по \"%s\": %s.", title, request.action());
        notificationService.createSystemNotification(listing.getOwner(), message);
    }

    private void notifyReviewResolution(Review review, ModerationResolutionRequest request) {
        if (review == null) {
            return;
        }
        User author = resolveReviewAuthor(review);
        if (author == null) {
            return;
        }
        String message = request.comment() != null && !request.comment().isBlank()
                ? String.format("Модерация по вашему отзыву: %s. Комментарий: %s.", request.action(), request.comment())
                : String.format("Модерация по вашему отзыву: %s.", request.action());
        notificationService.createSystemNotification(author, message);
    }

    private User resolveReviewAuthor(Review review) {
        if (review.getAuthorRole() == ReviewAuthorRole.LESSOR) {
            return review.getLessor();
        }
        if (review.getAuthorRole() == ReviewAuthorRole.LESSEE) {
            return review.getLessee();
        }
        return null;
    }
}
