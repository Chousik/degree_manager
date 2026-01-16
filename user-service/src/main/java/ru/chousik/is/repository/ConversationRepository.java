package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.chousik.is.entity.Conversation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    Optional<Conversation> findByRental_Id(UUID rentalId);

    @Query("""
        select c from Conversation c
        join ConversationPair cp on cp.conversation = c
        where c.listing.id = :listingId and cp.user.id = :userId
        """)
    Optional<Conversation> findByListingAndParticipant(@Param("listingId") UUID listingId, @Param("userId") UUID userId);

    @Query("""
        select distinct c from Conversation c
        join ConversationPair cp on cp.conversation = c
        where cp.user.id = :userId
        order by c.createdAt desc
        """)
    List<Conversation> findAllByParticipant(@Param("userId") UUID userId);
}
