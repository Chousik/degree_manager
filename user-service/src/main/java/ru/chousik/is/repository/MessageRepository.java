package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.is.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findAllByConversation_Rental_IdOrderBySentAtAsc(UUID rentalId);

    List<Message> findAllByConversation_IdOrderBySentAtAsc(UUID conversationId);

    Message findTopByConversation_IdOrderBySentAtDesc(UUID conversationId);
}
