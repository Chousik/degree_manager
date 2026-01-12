package ru.chousik.is.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chousik.is.admin.client.UserServiceClient;
import ru.chousik.is.admin.dto.listing.ListingArchiveRequest;
import ru.chousik.is.admin.dto.listing.ListingDetailDto;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/listings")
@RequiredArgsConstructor
public class AdminListingController {

    private final UserServiceClient userServiceClient;

    @GetMapping("/{listingId}")
    public ListingDetailDto getListing(@PathVariable UUID listingId,
                                       @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return userServiceClient.getListingDetail(listingId, authHeader);
    }

    @PostMapping("/{listingId}/archive")
    public ResponseEntity<Void> archiveListing(@PathVariable UUID listingId,
                                               @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                               @Valid @RequestBody ListingArchiveRequest request) {
        userServiceClient.archiveListing(listingId, request, authHeader);
        return ResponseEntity.noContent().build();
    }
}
