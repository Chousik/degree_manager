package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.is.entity.ListingCategory;
import ru.chousik.is.entity.ListingCategoryId;

import java.util.List;
import java.util.UUID;

public interface ListingCategoryRepository extends JpaRepository<ListingCategory, ListingCategoryId> {

    void deleteByListing_Id(UUID listingId);

    List<ListingCategory> findByListing_Id(UUID listingId);
}
