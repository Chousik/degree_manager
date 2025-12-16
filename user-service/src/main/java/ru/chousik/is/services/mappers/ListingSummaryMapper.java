package ru.chousik.is.services.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.chousik.is.dto.listing.ListingSummaryDto;
import ru.chousik.is.entity.Listing;
import ru.chousik.is.entity.ListingPhoto;
import ru.chousik.is.repository.ListingPhotoRepository;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ListingSummaryMapper {

    private static final Comparator<ListingPhoto> PREVIEW_COMPARATOR =
            Comparator.comparing(ListingPhoto::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(ListingPhoto::getId);

    private final ListingPhotoRepository listingPhotoRepository;

    public List<ListingSummaryDto> toSummaryList(List<Listing> listings) {
        if (CollectionUtils.isEmpty(listings)) {
            return Collections.emptyList();
        }
        Map<UUID, String> previewPhotoUrls = loadPreviewPhotoUrls(listings);
        return listings.stream()
                .map(listing -> toSummary(listing, previewPhotoUrls.get(listing.getId())))
                .toList();
    }

    public ListingSummaryDto toSummary(Listing listing) {
        return toSummary(listing, null);
    }

    private ListingSummaryDto toSummary(Listing listing, String previewUrl) {
        return new ListingSummaryDto(
                listing.getId(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getPricePerHour(),
                listing.getDepositAmount(),
                listing.getStatus(),
                listing.getLatitude(),
                listing.getLongitude(),
                previewUrl
        );
    }

    private Map<UUID, String> loadPreviewPhotoUrls(List<Listing> listings) {
        List<UUID> listingIds = listings.stream()
                .map(Listing::getId)
                .toList();
        List<ListingPhoto> photos = listingPhotoRepository.findByListing_IdIn(listingIds);
        Map<UUID, ListingPhoto> selected = new HashMap<>();
        for (ListingPhoto photo : photos) {
            UUID listingId = photo.getListing().getId();
            selected.merge(listingId, photo, (existing, candidate) ->
                    PREVIEW_COMPARATOR.compare(candidate, existing) < 0 ? candidate : existing);
        }
        return selected.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getUrl()));
    }
}
