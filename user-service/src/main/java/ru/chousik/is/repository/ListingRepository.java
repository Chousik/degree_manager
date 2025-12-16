package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.chousik.is.entity.Listing;
import ru.chousik.is.entity.ListingStatus;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID>, JpaSpecificationExecutor<Listing> {
    List<Listing> findAllByOwner_Id(UUID ownerId);

    List<Listing> findAllByOwner_IdAndStatusIn(UUID ownerId, Collection<ListingStatus> statuses);

    List<Listing> findAllByFlaggedTrue();
}
