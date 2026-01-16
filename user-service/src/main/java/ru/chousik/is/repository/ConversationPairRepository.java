package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.chousik.is.entity.ConversationPair;
import ru.chousik.is.entity.ConversationPairId;
import ru.chousik.is.entity.User;

import java.util.List;
import java.util.UUID;

public interface ConversationPairRepository extends JpaRepository<ConversationPair, ConversationPairId> {
    List<ConversationPair> findByConversation_Id(UUID conversationId);

    @Query("""
        select cp.user from ConversationPair cp
        where cp.conversation.id = :conversationId and cp.user.id <> :userId
        """)
    List<User> findOtherParticipants(@Param("conversationId") UUID conversationId, @Param("userId") UUID userId);
}
