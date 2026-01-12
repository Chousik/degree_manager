package ru.chousik.is.admin.client;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.chousik.is.admin.dto.moderation.FlaggedListingDto;
import ru.chousik.is.admin.dto.moderation.FlaggedReviewDto;
import ru.chousik.is.admin.dto.moderation.ModerationResolutionRequest;
import ru.chousik.is.admin.dto.report.ReportResolutionRequest;
import ru.chousik.is.admin.dto.report.ReportSummaryDto;
import ru.chousik.is.admin.dto.ban.BanCreateRequest;
import ru.chousik.is.admin.dto.ban.BanLiftRequest;
import ru.chousik.is.admin.dto.ban.BanResponse;
import ru.chousik.is.admin.dto.listing.ListingArchiveRequest;
import ru.chousik.is.admin.dto.listing.ListingDetailDto;
import ru.chousik.is.admin.dto.review.ReviewDetailDto;
import ru.chousik.is.admin.dto.review.ReviewHideRequest;
import ru.chousik.is.admin.dto.support.SupportTicketResolutionRequest;
import ru.chousik.is.admin.dto.support.SupportTicketResponse;
import ru.chousik.is.admin.dto.support.SupportTicketStatusRequest;

import java.util.List;
import java.util.UUID;

@Component
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient userServiceWebClient) {
        this.webClient = userServiceWebClient;
    }

    public List<FlaggedListingDto> getFlaggedListings(String authHeader) {
        return webClient.get()
                .uri("/api/moderation/listings")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToFlux(FlaggedListingDto.class)
                .collectList()
                .block();
    }

    public List<FlaggedReviewDto> getFlaggedReviews(String authHeader) {
        return webClient.get()
                .uri("/api/moderation/reviews")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToFlux(FlaggedReviewDto.class)
                .collectList()
                .block();
    }

    public void resolveListing(UUID listingId, ModerationResolutionRequest request, String authHeader) {
        webClient.post()
                .uri("/api/moderation/listings/{listingId}/resolve", listingId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void resolveReview(UUID reviewId, ModerationResolutionRequest request, String authHeader) {
        webClient.post()
                .uri("/api/moderation/reviews/{reviewId}/resolve", reviewId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public List<SupportTicketResponse> getOpenTickets(String authHeader) {
        return webClient.get()
                .uri("/api/support/tickets/open")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToFlux(SupportTicketResponse.class)
                .collectList()
                .block();
    }

    public List<SupportTicketResponse> getTickets(String status, String authHeader) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/support/tickets");
                    if (status != null && !status.isBlank()) {
                        builder.queryParam("status", status);
                    }
                    return builder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToFlux(SupportTicketResponse.class)
                .collectList()
                .block();
    }

    public SupportTicketResponse startTicket(UUID ticketId, SupportTicketStatusRequest request, String authHeader) {
        return webClient.post()
                .uri("/api/support/tickets/{ticketId}/start", ticketId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SupportTicketResponse.class)
                .block();
    }

    public SupportTicketResponse resolveTicket(UUID ticketId, SupportTicketResolutionRequest request, String authHeader) {
        return webClient.post()
                .uri("/api/support/tickets/{ticketId}/resolve", ticketId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(SupportTicketResponse.class)
                .block();
    }

    public List<ReportSummaryDto> getReports(String status, String authHeader) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/admin/reports");
                    if (status != null && !status.isBlank()) {
                        builder.queryParam("status", status);
                    }
                    return builder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToFlux(ReportSummaryDto.class)
                .collectList()
                .block();
    }

    public ReportSummaryDto resolveReport(UUID reportId, ReportResolutionRequest request, String authHeader) {
        return webClient.post()
                .uri("/api/admin/reports/{reportId}/resolve", reportId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ReportSummaryDto.class)
                .block();
    }

    public List<BanResponse> getBans(String status, String authHeader) {
        return webClient.get()
                .uri(uriBuilder -> {
                    var builder = uriBuilder.path("/api/admin/bans");
                    if (status != null && !status.isBlank()) {
                        builder.queryParam("status", status);
                    }
                    return builder.build();
                })
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToFlux(BanResponse.class)
                .collectList()
                .block();
    }

    public List<BanResponse> getBansForUser(UUID userId, String authHeader) {
        return webClient.get()
                .uri("/api/admin/bans/user/{userId}", userId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToFlux(BanResponse.class)
                .collectList()
                .block();
    }

    public BanResponse createBan(BanCreateRequest request, String authHeader) {
        return webClient.post()
                .uri("/api/admin/bans")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(BanResponse.class)
                .block();
    }

    public BanResponse liftBan(UUID banId, BanLiftRequest request, String authHeader) {
        return webClient.post()
                .uri("/api/admin/bans/{banId}/lift", banId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(BanResponse.class)
                .block();
    }

    public ListingDetailDto getListingDetail(UUID listingId, String authHeader) {
        return webClient.get()
                .uri("/api/admin/listings/{listingId}", listingId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(ListingDetailDto.class)
                .block();
    }

    public ReviewDetailDto getReviewDetail(UUID reviewId, String authHeader) {
        return webClient.get()
                .uri("/api/admin/reviews/{reviewId}", reviewId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .bodyToMono(ReviewDetailDto.class)
                .block();
    }

    public void hideReview(UUID reviewId, ReviewHideRequest request, String authHeader) {
        webClient.post()
                .uri("/api/admin/reviews/{reviewId}/hide", reviewId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    public void archiveListing(UUID listingId, ListingArchiveRequest request, String authHeader) {
        webClient.post()
                .uri("/api/admin/listings/{listingId}/archive", listingId)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
