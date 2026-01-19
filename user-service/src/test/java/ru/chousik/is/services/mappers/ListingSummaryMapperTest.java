package ru.chousik.is.services.mappers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.chousik.is.dto.listing.ListingSummaryDto;
import ru.chousik.is.entity.Listing;
import ru.chousik.is.entity.ListingPhoto;
import ru.chousik.is.repository.ListingPhotoRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListingSummaryMapperTest {

    @Mock
    private ListingPhotoRepository listingPhotoRepository;

    @InjectMocks
    private ListingSummaryMapper listingSummaryMapper;

    @Test
    void toSummaryList_selectsPreviewPhoto() {
        Listing listing = new Listing();
        listing.setId(UUID.randomUUID());
        listing.setTitle("Tool");
        listing.setDescription("Desc");
        listing.setPricePerHour(new BigDecimal("10.00"));

        Listing listing2 = new Listing();
        listing2.setId(UUID.randomUUID());
        listing2.setTitle("Camera");
        listing2.setDescription("Photo");
        listing2.setPricePerHour(new BigDecimal("20.00"));

        ListingPhoto photo1 = new ListingPhoto();
        photo1.setId(UUID.randomUUID());
        photo1.setListing(listing);
        photo1.setUrl("url-1");
        photo1.setSortOrder((short) 2);

        ListingPhoto photo2 = new ListingPhoto();
        photo2.setId(UUID.randomUUID());
        photo2.setListing(listing);
        photo2.setUrl("url-2");
        photo2.setSortOrder((short) 1);

        ListingPhoto photo3 = new ListingPhoto();
        photo3.setId(UUID.randomUUID());
        photo3.setListing(listing2);
        photo3.setUrl("url-3");
        photo3.setSortOrder((short) 1);

        when(listingPhotoRepository.findByListing_IdIn(List.of(listing.getId(), listing2.getId())))
                .thenReturn(List.of(photo1, photo2, photo3));

        List<ListingSummaryDto> result = listingSummaryMapper.toSummaryList(List.of(listing, listing2));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).previewPhotoUrl()).isEqualTo("url-2");
        assertThat(result.get(1).previewPhotoUrl()).isEqualTo("url-3");
    }

    @Test
    void toSummaryList_emptyInputReturnsEmptyList() {
        List<ListingSummaryDto> result = listingSummaryMapper.toSummaryList(List.of());

        assertThat(result).isEmpty();
    }
}
