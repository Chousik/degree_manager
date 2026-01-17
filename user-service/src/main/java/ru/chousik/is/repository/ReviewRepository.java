package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.chousik.is.entity.Review;
import ru.chousik.is.entity.ReviewAuthorRole;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    boolean existsByRental_IdAndAuthorRole(UUID rentalId, ReviewAuthorRole authorRole);

    List<Review> findAllByLessor_Id(UUID lessorId);

    List<Review> findAllByLessee_Id(UUID lesseeId);

    @Query("""
        select r from Review r
        where r.lessor.id = :lessorId and (r.hidden = false or r.hidden is null)
        """)
    List<Review> findAllVisibleByLessor(@Param("lessorId") UUID lessorId);

    @Query("""
        select r from Review r
        where r.lessee.id = :lesseeId and (r.hidden = false or r.hidden is null)
        """)
    List<Review> findAllVisibleByLessee(@Param("lesseeId") UUID lesseeId);

    @Query("""
        select r from Review r
        where r.lessor.id = :lessorId
          and r.authorRole = :authorRole
          and (r.hidden = false or r.hidden is null)
        order by r.createdAt desc
        """)
    List<Review> findAllVisibleByLessorAndAuthorRole(@Param("lessorId") UUID lessorId,
                                                     @Param("authorRole") ReviewAuthorRole authorRole);

    @Query("""
        select count(r) from Review r
        where r.lessor.id = :lessorId
          and r.authorRole = :authorRole
          and (r.hidden = false or r.hidden is null)
        """)
    long countVisibleByLessorAndAuthorRole(@Param("lessorId") UUID lessorId,
                                           @Param("authorRole") ReviewAuthorRole authorRole);

    List<Review> findAllByFlaggedTrue();
}
