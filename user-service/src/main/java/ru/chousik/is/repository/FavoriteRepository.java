package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.is.entity.Favorite;
import ru.chousik.is.entity.FavoriteId;

import java.util.List;
import java.util.UUID;

public interface FavoriteRepository extends JpaRepository<Favorite, FavoriteId> {

    boolean existsByUser_IdAndListing_Id(UUID userId, UUID listingId);

    void deleteByUser_IdAndListing_Id(UUID userId, UUID listingId);

    List<Favorite> findAllByUser_IdOrderByCreatedAtDesc(UUID userId);

}
