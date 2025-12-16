package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.is.entity.Review;
import ru.chousik.is.entity.ReviewAuthorRole;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    boolean existsByRental_IdAndAuthorRole(UUID rentalId, ReviewAuthorRole authorRole);

    List<Review> findAllByLessor_Id(UUID lessorId);

    List<Review> findAllByLessee_Id(UUID lesseeId);

    List<Review> findAllByFlaggedTrue();
}
