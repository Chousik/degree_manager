package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.is.entity.*;
import ru.chousik.is.repository.ConversationPairRepository;
import ru.chousik.is.repository.ConversationRepository;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationPairRepository conversationPairRepository;

    @Transactional
    public Conversation ensureConversation(Rental rental) {
        Conversation conversation = conversationRepository.findByRental_Id(rental.getId())
                .orElseGet(() -> {
                    Conversation created = new Conversation();
                    created.setRental(rental);
                    created.setCreatedAt(OffsetDateTime.now());
                    return conversationRepository.save(created);
                });
        addParticipant(conversation, rental.getLessor());
        addParticipant(conversation, rental.getLessee());
        return conversation;
    }

    public Conversation getConversation(Rental rental) {
        return conversationRepository.findByRental_Id(rental.getId())
                .orElseThrow(() -> new IllegalStateException("Conversation for rental %s not found".formatted(rental.getId())));
    }

    private void addParticipant(Conversation conversation, User user) {
        if (user == null) {
            return;
        }
        ConversationPairId id = new ConversationPairId();
        id.setConversationId(conversation.getId());
        id.setUserId(user.getId());
        if (conversationPairRepository.existsById(id)) {
            return;
        }
        ConversationPair pair = new ConversationPair();
        pair.setId(id);
        pair.setConversation(conversation);
        pair.setUser(user);
        pair.setCreatedAt(OffsetDateTime.now());
        conversationPairRepository.save(pair);
    }
}
