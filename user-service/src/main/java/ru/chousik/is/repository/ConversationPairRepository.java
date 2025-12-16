package ru.chousik.is.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.chousik.is.entity.ConversationPair;
import ru.chousik.is.entity.ConversationPairId;

public interface ConversationPairRepository extends JpaRepository<ConversationPair, ConversationPairId> {
}
