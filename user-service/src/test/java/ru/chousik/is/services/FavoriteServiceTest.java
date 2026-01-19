package ru.chousik.is.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ListingRepository listingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ListingSummaryMapper listingSummaryMapper;

    @InjectMocks
    private FavoriteService favoriteService;

    @Test
    void addFavorite_skipsWhenAlreadyExists() {
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        Listing listing = new Listing();
        listing.setId(listingId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(favoriteRepository.existsByUser_IdAndListing_Id(userId, listingId)).thenReturn(true);

        favoriteService.addFavorite(new FavoriteRequest(userId, listingId));

        verify(favoriteRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getFavorites_missingUserThrows() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> favoriteService.getFavorites(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getFavorites_returnsSummaryList() {
        UUID userId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(true);

        Favorite favorite = new Favorite();
        FavoriteId favoriteId = new FavoriteId();
        favoriteId.setUserId(userId);
        favoriteId.setListingId(listingId);
        favorite.setId(favoriteId);
        when(favoriteRepository.findAllByUser_IdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(favorite));

        Listing listing = new Listing();
        listing.setId(listingId);
        listing.setTitle("Tool");
        listing.setPricePerHour(new BigDecimal("10.00"));
        when(listingRepository.findAllById(List.of(listingId)))
                .thenReturn(List.of(listing));

        ListingSummaryDto summary = new ListingSummaryDto(
                listingId,
                "Tool",
                null,
                new BigDecimal("10.00"),
                null,
                null,
                null,
                null,
                null,
                null
        );
        when(listingSummaryMapper.toSummaryList(List.of(listing))).thenReturn(List.of(summary));

        List<ListingSummaryDto> result = favoriteService.getFavorites(userId);

        assertThat(result).containsExactly(summary);
    }
}
