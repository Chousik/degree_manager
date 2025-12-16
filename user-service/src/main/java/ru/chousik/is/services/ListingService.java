package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.chousik.is.dto.listing.*;
import ru.chousik.is.entity.AvailabilitySlot;
import ru.chousik.is.entity.Category;
import ru.chousik.is.entity.Listing;
import ru.chousik.is.entity.ListingCategory;
import ru.chousik.is.entity.ListingCategoryId;
import ru.chousik.is.entity.ListingPhoto;
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
import ru.chousik.is.services.specifications.ListingSpecifications;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListingService {

    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final AvailabilitySlotRepository availabilitySlotRepository;
    private final ListingPhotoRepository listingPhotoRepository;
    private final CategoryRepository categoryRepository;
    private final ListingCategoryRepository listingCategoryRepository;
    private final ListingSummaryMapper listingSummaryMapper;

    @Transactional
    public ListingResponse createListing(ListingCreateRequest request) {
        validateAvailabilitySlots(request.availabilitySlots());
        User owner = userRepository.findById(request.ownerId())
                .orElseThrow(() -> new ResourceNotFoundException("User %s not found".formatted(request.ownerId())));

        Listing listing = new Listing();
        listing.setOwner(owner);
        listing.setTitle(request.title());
        listing.setDescription(request.description());
        listing.setPricePerHour(request.pricePerHour());
        listing.setDepositAmount(request.depositAmount());
        listing.setAutoConfirmation(Boolean.TRUE.equals(request.autoConfirmation()));
        listing.setStatus(ListingStatus.AVAILABLE);
        listing.setLatitude(request.latitude());
        listing.setLongitude(request.longitude());
        listing.setCreatedAt(OffsetDateTime.now());

        Listing savedListing = listingRepository.save(listing);

        List<AvailabilitySlot> slots = persistAvailabilitySlots(savedListing, request.availabilitySlots());
        List<ListingPhoto> photos = persistPhotos(savedListing, request.photos());
        List<Category> categories = persistCategories(savedListing, request.categoryIds());

        return mapToResponse(savedListing, slots, photos, categories);
    }

    @Transactional
    public ListingResponse updateListing(UUID listingId, ListingUpdateRequest request) {
        validateAvailabilitySlots(request.availabilitySlots());
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing %s not found".formatted(listingId)));

        assertOwner(listing, request.ownerId());

        listing.setTitle(request.title());
        listing.setDescription(request.description());
        listing.setPricePerHour(request.pricePerHour());
        listing.setDepositAmount(request.depositAmount());
        listing.setAutoConfirmation(Boolean.TRUE.equals(request.autoConfirmation()));
        if (request.status() != null) {
            listing.setStatus(request.status());
        }
        listing.setLatitude(request.latitude());
        listing.setLongitude(request.longitude());

        Listing savedListing = listingRepository.save(listing);

        availabilitySlotRepository.deleteByListing_Id(listingId);
        listingPhotoRepository.deleteByListing_Id(listingId);
        listingCategoryRepository.deleteByListing_Id(listingId);

        List<AvailabilitySlot> slots = persistAvailabilitySlots(savedListing, request.availabilitySlots());
        List<ListingPhoto> photos = persistPhotos(savedListing, request.photos());
        List<Category> categories = persistCategories(savedListing, request.categoryIds());

        return mapToResponse(savedListing, slots, photos, categories);
    }

    @Transactional
    public void deleteListing(UUID listingId, UUID ownerId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing %s not found".formatted(listingId)));
        assertOwner(listing, ownerId);
        listingRepository.delete(listing);
    }

    @Transactional(readOnly = true)
    public Page<ListingSummaryDto> searchListings(ListingSearchRequest request, Pageable pageable) {
        Specification<Listing> specification = ListingSpecifications.fromRequest(request);
        Page<Listing> listings = listingRepository.findAll(specification, pageable);
        List<ListingSummaryDto> summaries = listingSummaryMapper.toSummaryList(listings.getContent());
        return new PageImpl<>(summaries, pageable, listings.getTotalElements());
    }

    @Transactional(readOnly = true)
    public List<ListingMapPoint> getListingsForMap(ListingSearchRequest request) {
        Specification<Listing> specification = ListingSpecifications.fromRequest(request);
        return listingRepository.findAll(specification).stream()
                .filter(listing -> listing.getLatitude() != null && listing.getLongitude() != null)
                .map(listing -> new ListingMapPoint(
                        listing.getId(),
                        listing.getTitle(),
                        listing.getPricePerHour(),
                        listing.getLatitude(),
                        listing.getLongitude()
                ))
                .toList();
    }

    private void validateAvailabilitySlots(List<AvailabilitySlotRequest> slots) {
        if (slots == null) {
            return;
        }
        for (AvailabilitySlotRequest slot : slots) {
            if (slot.startsAt() != null && slot.endsAt() != null && !slot.endsAt().isAfter(slot.startsAt())) {
                throw new BusinessValidationException("Slot end time must be after start time");
            }
        }
    }

    private List<AvailabilitySlot> persistAvailabilitySlots(Listing listing, List<AvailabilitySlotRequest> slots) {
        if (CollectionUtils.isEmpty(slots)) {
            return List.of();
        }
        List<AvailabilitySlot> entities = new ArrayList<>();
        for (AvailabilitySlotRequest slot : slots) {
            AvailabilitySlot entity = new AvailabilitySlot();
            entity.setListing(listing);
            entity.setStartsAt(slot.startsAt());
            entity.setEndsAt(slot.endsAt());
            entity.setNote(slot.note());
            entities.add(entity);
        }
        return availabilitySlotRepository.saveAll(entities);
    }

    private List<ListingPhoto> persistPhotos(Listing listing, List<ListingPhotoRequest> photos) {
        if (CollectionUtils.isEmpty(photos)) {
            return List.of();
        }
        List<ListingPhoto> entities = new ArrayList<>();
        for (ListingPhotoRequest request : photos) {
            ListingPhoto photo = new ListingPhoto();
            photo.setListing(listing);
            photo.setUrl(request.url());
            photo.setSortOrder(request.sortOrder());
            entities.add(photo);
        }
        return listingPhotoRepository.saveAll(entities);
    }

    private List<Category> persistCategories(Listing listing, List<UUID> categoryIds) {
        if (CollectionUtils.isEmpty(categoryIds)) {
            return List.of();
        }
        Set<UUID> uniqueIds = new LinkedHashSet<>(categoryIds);
        List<Category> categories = categoryRepository.findAllById(uniqueIds);
        if (categories.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException("One or more categories were not found");
        }

        var categoryMap = categories.stream()
                .collect(Collectors.toMap(Category::getId, category -> category));

        List<Category> persistedCategories = new ArrayList<>();
        for (UUID categoryId : uniqueIds) {
            Category category = categoryMap.get(categoryId);
            ListingCategoryId id = new ListingCategoryId();
            id.setListingId(listing.getId());
            id.setCategoryId(category.getId());

            ListingCategory listingCategory = new ListingCategory();
            listingCategory.setId(id);
            listingCategory.setListing(listing);
            listingCategory.setCategory(category);
            listingCategoryRepository.save(listingCategory);
            persistedCategories.add(category);
        }
        return persistedCategories;
    }

    private ListingResponse mapToResponse(Listing listing,
                                          List<AvailabilitySlot> slots,
                                          List<ListingPhoto> photos,
                                          List<Category> categories) {
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
                .map(category -> new CategorySummaryDto(category.getId(), category.getName()))
                .toList();

        return new ListingResponse(
                listing.getId(),
                listing.getOwner().getId(),
                listing.getTitle(),
                listing.getDescription(),
                listing.getPricePerHour(),
                listing.getDepositAmount(),
                Boolean.TRUE.equals(listing.getAutoConfirmation()),
                listing.getStatus(),
                listing.getLatitude(),
                listing.getLongitude(),
                listing.getCreatedAt(),
                slotDtos,
                photoDtos,
                categoryDtos
        );
    }

    private void assertOwner(Listing listing, UUID ownerId) {
        if (ownerId == null || !listing.getOwner().getId().equals(ownerId)) {
            throw new BusinessValidationException("Operation allowed only for listing owner");
        }
    }
}
