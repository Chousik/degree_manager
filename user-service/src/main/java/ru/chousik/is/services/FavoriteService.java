package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.is.dto.favorite.FavoriteRequest;
import ru.chousik.is.dto.listing.ListingSummaryDto;
import ru.chousik.is.entity.Favorite;
import ru.chousik.is.entity.FavoriteId;
import ru.chousik.is.entity.Listing;
import ru.chousik.is.entity.User;
import ru.chousik.is.exceptions.ResourceNotFoundException;
import ru.chousik.is.repository.FavoriteRepository;
import ru.chousik.is.repository.ListingRepository;
import ru.chousik.is.repository.UserRepository;
import ru.chousik.is.services.mappers.ListingSummaryMapper;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ListingSummaryMapper listingSummaryMapper;

    @Transactional
    public void addFavorite(FavoriteRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User %s not found".formatted(request.userId())));
        Listing listing = listingRepository.findById(request.listingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing %s not found".formatted(request.listingId())));

        boolean exists = favoriteRepository.existsByUser_IdAndListing_Id(request.userId(), request.listingId());
        if (exists) {
            return;
        }

        Favorite favorite = new Favorite();
        FavoriteId id = new FavoriteId();
        id.setUserId(user.getId());
        id.setListingId(listing.getId());
        favorite.setId(id);
        favorite.setUser(user);
        favorite.setListing(listing);
        favorite.setCreatedAt(OffsetDateTime.now());
        favoriteRepository.save(favorite);
    }

    @Transactional
    public void removeFavorite(UUID userId, UUID listingId) {
        favoriteRepository.deleteByUser_IdAndListing_Id(userId, listingId);
    }

    @Transactional(readOnly = true)
    public List<ListingSummaryDto> getFavorites(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User %s not found".formatted(userId));
        }
        List<Favorite> favorites = favoriteRepository.findAllByUser_IdOrderByCreatedAtDesc(userId);
        List<UUID> listingIds = favorites.stream()
                .map(favorite -> favorite.getId().getListingId())
                .toList();
        if (listingIds.isEmpty()) {
            return List.of();
        }
        List<Listing> listings = listingRepository.findAllById(listingIds);
        Map<UUID, Listing> listingMap = listings.stream()
                .collect(Collectors.toMap(Listing::getId, listing -> listing));
        List<Listing> orderedListings = listingIds.stream()
                .map(listingMap::get)
                .filter(listing -> listing != null)
                .toList();
        return listingSummaryMapper.toSummaryList(orderedListings);
    }
}
