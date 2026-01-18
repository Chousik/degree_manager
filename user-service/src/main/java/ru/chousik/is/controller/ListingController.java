package ru.chousik.is.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.chousik.is.dto.listing.*;
import ru.chousik.is.services.ListingService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/listings")
@Validated
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @GetMapping
    public ResponseEntity<Page<ListingSummaryDto>> searchListings(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) String availableFrom,
            @RequestParam(required = false) String availableTo,
            @RequestParam(required = false) BigDecimal minLatitude,
            @RequestParam(required = false) BigDecimal maxLatitude,
            @RequestParam(required = false) BigDecimal minLongitude,
            @RequestParam(required = false) BigDecimal maxLongitude,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        ListingSearchRequest request = new ListingSearchRequest(
                text,
                categoryId,
                parseDecimal(minPrice),
                parseDecimal(maxPrice),
                parseDateTime(availableFrom),
                parseDateTime(availableTo),
                minLatitude,
                maxLatitude,
                minLongitude,
                maxLongitude
        );
        Page<ListingSummaryDto> page = listingService.searchListings(request, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{listingId}")
    public ResponseEntity<ListingResponse> getListing(@PathVariable UUID listingId) {
        ListingResponse response = listingService.getListing(listingId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/map")
    public ResponseEntity<List<ListingMapPoint>> getListingsForMap(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String minPrice,
            @RequestParam(required = false) String maxPrice,
            @RequestParam(required = false) String availableFrom,
            @RequestParam(required = false) String availableTo,
            @RequestParam(required = false) BigDecimal minLatitude,
            @RequestParam(required = false) BigDecimal maxLatitude,
            @RequestParam(required = false) BigDecimal minLongitude,
            @RequestParam(required = false) BigDecimal maxLongitude
    ) {
        ListingSearchRequest request = new ListingSearchRequest(
                text,
                categoryId,
                parseDecimal(minPrice),
                parseDecimal(maxPrice),
                parseDateTime(availableFrom),
                parseDateTime(availableTo),
                minLatitude,
                maxLatitude,
                minLongitude,
                maxLongitude
        );
        List<ListingMapPoint> points = listingService.getListingsForMap(request);
        return ResponseEntity.ok(points);
    }

    private BigDecimal parseDecimal(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().replace(" ", "").replace(",", ".");
        try {
            return new BigDecimal(normalized).setScale(10, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private OffsetDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return OffsetDateTime.parse(value.trim());
        } catch (DateTimeParseException ex) {
            try {
                LocalDate date = LocalDate.parse(value.trim());
                return date.atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    @PostMapping
    public ResponseEntity<ListingResponse> createListing(@Valid @RequestBody ListingCreateRequest request) {
        ListingResponse response = listingService.createListing(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{listingId}")
    public ResponseEntity<ListingResponse> updateListing(@PathVariable UUID listingId,
                                                         @Valid @RequestBody ListingUpdateRequest request) {
        ListingResponse response = listingService.updateListing(listingId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{listingId}")
    public ResponseEntity<Void> deleteListing(@PathVariable UUID listingId,
                                              @RequestParam UUID ownerId) {
        listingService.deleteListing(listingId, ownerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{listingId}/archive")
    public ResponseEntity<ListingResponse> archiveListing(@PathVariable UUID listingId,
                                                          @RequestParam UUID ownerId) {
        ListingResponse response = listingService.archiveListing(listingId, ownerId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{listingId}/unarchive")
    public ResponseEntity<ListingResponse> unarchiveListing(@PathVariable UUID listingId,
                                                            @RequestParam UUID ownerId) {
        ListingResponse response = listingService.unarchiveListing(listingId, ownerId);
        return ResponseEntity.ok(response);
    }
}
