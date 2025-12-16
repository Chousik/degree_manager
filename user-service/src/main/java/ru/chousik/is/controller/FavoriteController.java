package ru.chousik.is.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.chousik.is.dto.favorite.FavoriteRequest;
import ru.chousik.is.dto.listing.ListingSummaryDto;
import ru.chousik.is.services.FavoriteService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/favorites")
@Validated
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    public ResponseEntity<Void> addFavorite(@Valid @RequestBody FavoriteRequest request) {
        favoriteService.addFavorite(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{listingId}")
    public ResponseEntity<Void> removeFavorite(@PathVariable UUID listingId,
                                               @RequestParam @NotNull UUID userId) {
        favoriteService.removeFavorite(userId, listingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ListingSummaryDto>> getFavorites(@RequestParam @NotNull UUID userId) {
        List<ListingSummaryDto> favorites = favoriteService.getFavorites(userId);
        return ResponseEntity.ok(favorites);
    }
}
