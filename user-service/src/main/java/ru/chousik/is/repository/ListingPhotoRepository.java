package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.is.entity.ListingPhoto;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ListingPhotoRepository extends JpaRepository<ListingPhoto, UUID> {

    void deleteByListing_Id(UUID listingId);

    List<ListingPhoto> findByListing_IdIn(Collection<UUID> listingIds);

    List<ListingPhoto> findByListing_Id(UUID listingId);
}
