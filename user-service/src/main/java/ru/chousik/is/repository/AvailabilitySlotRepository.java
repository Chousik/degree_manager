package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.is.entity.AvailabilitySlot;

import java.util.List;
import java.util.UUID;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, UUID> {

    void deleteByListing_Id(UUID listingId);

    List<AvailabilitySlot> findByListing_Id(UUID listingId);

    List<AvailabilitySlot> findAllByListing_IdIn(List<UUID> listingIds);
}
