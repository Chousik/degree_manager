package ru.chousik.is.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.chousik.is.dto.listing.AvailabilitySlotRequest;
import ru.chousik.is.dto.listing.ListingCreateRequest;
import ru.chousik.is.dto.listing.ListingPhotoRequest;
import ru.chousik.is.dto.listing.ListingUpdateRequest;
import ru.chousik.is.entity.Category;
import ru.chousik.is.entity.Listing;
import ru.chousik.is.entity.ListingStatus;
import ru.chousik.is.entity.User;
import ru.chousik.is.exceptions.BusinessValidationException;
import ru.chousik.is.exceptions.ResourceNotFoundException;
import ru.chousik.is.repository.AvailabilitySlotRepository;
import ru.chousik.is.repository.CategoryRepository;
import ru.chousik.is.repository.ListingCategoryRepository;
import ru.chousik.is.repository.ListingPhotoRepository;
import ru.chousik.is.repository.ListingRepository;
import ru.chousik.is.repository.UserRepository;
import ru.chousik.is.services.mappers.ListingSummaryMapper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListingServiceTest {

    @Mock
    private ListingRepository listingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AvailabilitySlotRepository availabilitySlotRepository;
    @Mock
    private ListingPhotoRepository listingPhotoRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ListingCategoryRepository listingCategoryRepository;
    @Mock
    private ListingSummaryMapper listingSummaryMapper;

    @InjectMocks
    private ListingService listingService;

    @Test
    void createListing_persistsAggregate() {
        UUID ownerId = UUID.randomUUID();
        UUID listingId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        User owner = new User();
        owner.setId(ownerId);
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        when(listingRepository.save(any(Listing.class))).thenAnswer(invocation -> {
            Listing listing = invocation.getArgument(0);
            listing.setId(listingId);
            return listing;
        });

        when(availabilitySlotRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(listingPhotoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Power tools");
        when(categoryRepository.findAllById(any())).thenReturn(List.of(category));
        when(listingCategoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ListingCreateRequest request = new ListingCreateRequest(
                ownerId,
                "Перфоратор",
                "Makita HR2470",
                new BigDecimal("10.00"),
                new BigDecimal("100"),
                true,
                new BigDecimal("55.7558"),
                new BigDecimal("37.6173"),
                List.of(new AvailabilitySlotRequest(
                        OffsetDateTime.now().plusDays(1),
                        OffsetDateTime.now().plusDays(1).plusHours(2),
                        "утро"
                )),
                List.of(new ListingPhotoRequest("https://cdn/img.jpg", (short) 1)),
                List.of(categoryId)
        );

        var response = listingService.createListing(request);

        assertThat(response.id()).isEqualTo(listingId);
        assertThat(response.ownerId()).isEqualTo(ownerId);
        assertThat(response.autoConfirmation()).isTrue();
        assertThat(response.availabilitySlots()).hasSize(1);
        assertThat(response.photos()).hasSize(1);
        assertThat(response.categories()).extracting("id").containsExactly(categoryId);

    }

    @Test
    void updateListing_rewritesAggregate() {
        UUID listingId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        User owner = new User();
        owner.setId(ownerId);

        Listing listing = new Listing();
        listing.setId(listingId);
        listing.setOwner(owner);
        listing.setStatus(ListingStatus.AVAILABLE);

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));
        when(listingRepository.save(any(Listing.class))).thenAnswer(inv -> inv.getArgument(0));
        when(availabilitySlotRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(listingPhotoRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Category category = new Category();
        category.setId(categoryId);
        category.setName("Garden");
        when(categoryRepository.findAllById(any())).thenReturn(List.of(category));

        ListingUpdateRequest request = new ListingUpdateRequest(
                ownerId,
                "Лобзик",
                "Bosch",
                new BigDecimal("12.5"),
                BigDecimal.ZERO,
                false,
                null,
                null,
                List.of(new AvailabilitySlotRequest(
                        OffsetDateTime.now().plusDays(2),
                        OffsetDateTime.now().plusDays(2).plusHours(1),
                        null
                )),
                List.of(new ListingPhotoRequest("https://cdn/img2.jpg", null)),
                List.of(categoryId),
                ListingStatus.ARCHIVED
        );

        var response = listingService.updateListing(listingId, request);

        assertThat(response.status()).isEqualTo(ListingStatus.ARCHIVED);
        assertThat(response.title()).isEqualTo("Лобзик");
        assertThat(response.photos()).hasSize(1);

        verify(availabilitySlotRepository).deleteByListing_Id(listingId);
        verify(listingPhotoRepository).deleteByListing_Id(listingId);
        verify(listingCategoryRepository).deleteByListing_Id(listingId);
    }

    @Test
    void updateListing_wrongOwner() {
        UUID listingId = UUID.randomUUID();
        Listing listing = new Listing();
        User owner = new User();
        owner.setId(UUID.randomUUID());
        listing.setOwner(owner);

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        ListingUpdateRequest request = new ListingUpdateRequest(
                UUID.randomUUID(),
                "Title",
                null,
                new BigDecimal("1"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> listingService.updateListing(listingId, request))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void deleteListing_requiresOwner() {
        UUID listingId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Listing listing = new Listing();
        User owner = new User();
        owner.setId(ownerId);
        listing.setOwner(owner);

        when(listingRepository.findById(listingId)).thenReturn(Optional.of(listing));

        listingService.deleteListing(listingId, ownerId);

        verify(listingRepository).delete(listing);
    }

    @Test
    void createListing_missingOwnerThrows() {
        UUID ownerId = UUID.randomUUID();
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        ListingCreateRequest request = new ListingCreateRequest(
                ownerId,
                "Перфоратор",
                "",
                new BigDecimal("10.00"),
                null,
                false,
                null,
                null,
                null,
                null,
                null
        );

        assertThatThrownBy(() -> listingService.createListing(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }
}
