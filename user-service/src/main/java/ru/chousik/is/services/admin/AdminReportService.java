package ru.chousik.is.services.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.is.dto.admin.report.ReportResolutionRequest;
import ru.chousik.is.dto.admin.report.ReportSummaryDto;
import ru.chousik.is.entity.Listing;
import ru.chousik.is.entity.ModerationAction;
import ru.chousik.is.entity.Report;
import ru.chousik.is.entity.Review;
import ru.chousik.is.entity.User;
import ru.chousik.is.exceptions.ResourceNotFoundException;
import ru.chousik.is.repository.ListingRepository;
import ru.chousik.is.repository.ModerationActionRepository;
import ru.chousik.is.repository.ReportRepository;
import ru.chousik.is.repository.ReviewRepository;
import ru.chousik.is.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ModerationActionRepository moderationActionRepository;
    private final ListingRepository listingRepository;
    private final ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public List<ReportSummaryDto> getReports(String status) {
        List<Report> reports = status == null || status.isBlank()
                ? reportRepository.findAll()
                : reportRepository.findAllByStatusIgnoreCase(status);

        return reports.stream().map(this::mapSummary).toList();
    }


    @Transactional
    public ReportSummaryDto resolveReport(UUID reportId, ReportResolutionRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report %s not found".formatted(reportId)));
        User admin = userRepository.findById(request.adminId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin %s not found".formatted(request.adminId())));

        report.setStatus(request.status());
        report.setResolvedBy(admin);
        reportRepository.save(report);

        String targetType = report.getTargetType();
        UUID targetId = report.getTargetId();
        if (targetId != null && targetType != null) {
            if ("LISTING".equalsIgnoreCase(targetType)) {
                clearListingFlagIfNoOpenReports(targetId);
            } else if ("REVIEW".equalsIgnoreCase(targetType)) {
                clearReviewFlagIfNoOpenReports(targetId);
            }
        }

        ModerationAction moderationAction = new ModerationAction();
        moderationAction.setReport(report);
        moderationAction.setActor(admin);
        moderationAction.setTargetUser(report.getReporter());
        moderationAction.setAction("resolve_report");
        moderationAction.setComment(request.comment());
        moderationAction.setCreatedAt(OffsetDateTime.now());
        moderationActionRepository.save(moderationAction);

        return mapSummary(report);
    }

    private ReportSummaryDto mapSummary(Report report) {
        return new ReportSummaryDto(
                report.getId(),
                report.getReporter() != null ? report.getReporter().getId() : null,
                report.getStatus(),
                report.getTargetType(),
                report.getTargetId(),
                report.getReasonBody(),
                getModeratorComment(report.getId()),
                report.getCreatedAt(),
                report.getResolvedBy() != null ? report.getResolvedBy().getId() : null
        );
    }

    private String getModeratorComment(UUID reportId) {
        return moderationActionRepository.findTopByReport_IdOrderByCreatedAtDesc(reportId)
                .map(ModerationAction::getComment)
                .orElse(null);
    }

    private void clearListingFlagIfNoOpenReports(UUID listingId) {
        if (!reportRepository.findAllByTargetTypeIgnoreCaseAndTargetIdAndStatusIgnoreCase("LISTING", listingId, "OPEN").isEmpty()) {
            return;
        }
        Listing listing = listingRepository.findById(listingId).orElse(null);
        if (listing == null) {
            return;
        }
        listing.setFlagged(Boolean.FALSE);
        listing.setFlagReason(null);
        listingRepository.save(listing);
    }

    private void clearReviewFlagIfNoOpenReports(UUID reviewId) {
        if (!reportRepository.findAllByTargetTypeIgnoreCaseAndTargetIdAndStatusIgnoreCase("REVIEW", reviewId, "OPEN").isEmpty()) {
            return;
        }
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) {
            return;
        }
        review.setFlagged(Boolean.FALSE);
        review.setFlagReason(null);
        reviewRepository.save(review);
    }
}
