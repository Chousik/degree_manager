package ru.chousik.is.services.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.is.dto.admin.listing.ListingArchiveRequest;
import ru.chousik.is.dto.admin.listing.ListingDetailDto;
import ru.chousik.is.dto.listing.AvailabilitySlotDto;
import ru.chousik.is.dto.listing.CategorySummaryDto;
import ru.chousik.is.dto.listing.ListingPhotoDto;
import ru.chousik.is.entity.AvailabilitySlot;
import ru.chousik.is.entity.Category;
import ru.chousik.is.entity.Listing;
import ru.chousik.is.entity.ListingStatus;
import ru.chousik.is.entity.ListingCategory;
import ru.chousik.is.entity.ListingPhoto;
import ru.chousik.is.entity.ModerationAction;
import ru.chousik.is.entity.User;
import ru.chousik.is.exceptions.ResourceNotFoundException;
import ru.chousik.is.repository.AvailabilitySlotRepository;
import ru.chousik.is.repository.ListingRepository;
import ru.chousik.is.repository.ListingCategoryRepository;
import ru.chousik.is.repository.ListingPhotoRepository;
import ru.chousik.is.repository.ModerationActionRepository;
import ru.chousik.is.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminListingService {

    private final ListingRepository listingRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final ListingPhotoRepository listingPhotoRepository;
    private final ListingCategoryRepository listingCategoryRepository;
    private final UserRepository userRepository;
    private final ModerationActionRepository moderationActionRepository;

    @Transactional(readOnly = true)
    public ListingDetailDto getListingDetail(UUID listingId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing %s not found".formatted(listingId)));

        List<AvailabilitySlot> slots = availabilitySlotRepository.findByListing_Id(listingId);
        List<ListingPhoto> photos = listingPhotoRepository.findByListing_Id(listingId);
        List<Category> categories = listingCategoryRepository.findByListing_Id(listingId).stream()
                .map(ListingCategory::getCategory)
                .toList();

        List<AvailabilitySlotDto> slotDtos = slots.stream()
                .map(slot -> new AvailabilitySlotDto(
                        slot.getId(),
                        slot.getStartsAt(),
                        slot.getEndsAt(),
                        slot.getNote()
                ))
                .toList();

        List<ListingPhotoDto> photoDtos = photos.stream()
                .map(photo -> new ListingPhotoDto(
                        photo.getId(),
                        photo.getUrl(),
                        photo.getSortOrder()
                ))
                .toList();

        List<CategorySummaryDto> categoryDtos = categories.stream()
                .map(category -> new CategorySummaryDto(
                        category.getId(),
                        category.getName()
                ))
                .toList();

        return new ListingDetailDto(
                listing.getId(),
                listing.getOwner() != null ? listing.getOwner().getId() : null,
                listing.getTitle(),
                listing.getDescription(),
                listing.getPricePerHour(),
                listing.getDepositAmount(),
                Boolean.TRUE.equals(listing.getAutoConfirmation()),
                listing.getStatus(),
                listing.getLatitude(),
                listing.getLongitude(),
                listing.getAddress(),
                listing.getCreatedAt(),
                slotDtos,
                photoDtos,
                categoryDtos,
                listing.getFlagged(),
                listing.getFlagReason()
        );
    }

    @Transactional
    public void archiveListing(UUID listingId, ListingArchiveRequest request) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing %s not found".formatted(listingId)));
        User admin = userRepository.findById(request.adminId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin %s not found".formatted(request.adminId())));

        listing.setStatus(ListingStatus.ARCHIVED);
        listing.setFlagged(Boolean.TRUE);
        listing.setFlagReason(request.comment());
        listingRepository.save(listing);

        ModerationAction moderationAction = new ModerationAction();
        moderationAction.setActor(admin);
        moderationAction.setListing(listing);
        moderationAction.setTargetUser(listing.getOwner());
        moderationAction.setAction("archive_listing");
        moderationAction.setComment(request.comment());
        moderationAction.setCreatedAt(OffsetDateTime.now());
        moderationActionRepository.save(moderationAction);
    }
}
