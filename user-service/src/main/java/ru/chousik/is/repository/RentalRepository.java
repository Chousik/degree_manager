package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.is.entity.Rental;
import ru.chousik.is.entity.RentalStatus;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface RentalRepository extends JpaRepository<Rental, UUID> {
    List<Rental> findByLessor_IdAndStatusInOrderByStartAtAsc(UUID lessorId, Collection<RentalStatus> statuses);
    boolean existsByListing_IdAndStatusIn(UUID listingId, Collection<RentalStatus> statuses);
    List<Rental> findByListing_IdAndStatusInOrderByStartAtAsc(UUID listingId, Collection<RentalStatus> statuses);
    boolean existsByListing_IdAndStatusInAndStartAtLessThanAndEndAtGreaterThan(
            UUID listingId,
            Collection<RentalStatus> statuses,
            java.time.OffsetDateTime endAt,
            java.time.OffsetDateTime startAt
    );
    List<Rental> findByLessor_IdOrderByStartAtDesc(UUID lessorId);
    List<Rental> findByLessee_IdOrderByStartAtDesc(UUID lesseeId);
}
