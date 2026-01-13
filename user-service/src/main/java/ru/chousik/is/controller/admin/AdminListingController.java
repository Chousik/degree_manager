package ru.chousik.is.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chousik.is.dto.admin.listing.ListingArchiveRequest;
import ru.chousik.is.dto.admin.listing.ListingDetailDto;
import ru.chousik.is.services.admin.AdminListingService;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/listings")
@RequiredArgsConstructor
public class AdminListingController {

    private final AdminListingService adminListingService;

    @GetMapping("/{listingId}")
    public ListingDetailDto getListing(@PathVariable UUID listingId) {
        return adminListingService.getListingDetail(listingId);
    }

    @PostMapping("/{listingId}/archive")
    public ResponseEntity<Void> archiveListing(@PathVariable UUID listingId,
                                               @Valid @RequestBody ListingArchiveRequest request) {
        adminListingService.archiveListing(listingId, request);
        return ResponseEntity.noContent().build();
    }
}
