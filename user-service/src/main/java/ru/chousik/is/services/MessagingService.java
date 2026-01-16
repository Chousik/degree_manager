package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.is.dto.message.MessageDto;
import ru.chousik.is.dto.message.MessageRequest;
import ru.chousik.is.entity.Conversation;
import ru.chousik.is.entity.Message;
import ru.chousik.is.entity.Rental;
import ru.chousik.is.entity.User;
import ru.chousik.is.exceptions.BusinessValidationException;
import ru.chousik.is.repository.MessageRepository;
import ru.chousik.is.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MessagingService {

    private final RentalService rentalService;
    private final ConversationService conversationService;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Transactional
    public MessageDto sendMessage(UUID rentalId, MessageRequest request) {
        Rental rental = rentalService.findRental(rentalId);
        validateParticipant(rental, request.senderId());
        var conversation = conversationService.ensureConversation(rental);

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(resolveUser(rental, request.senderId()));
        message.setBody(request.body());
        message.setSentAt(OffsetDateTime.now());
        message.setIsRead(Boolean.FALSE);
        Message saved = messageRepository.save(message);
        return map(saved);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessages(UUID rentalId, UUID requesterId) {
        Rental rental = rentalService.findRental(rentalId);
        validateParticipant(rental, requesterId);
        return messageRepository.findAllByConversation_Rental_IdOrderBySentAtAsc(rentalId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional
    public MessageDto sendConversationMessage(UUID conversationId, MessageRequest request) {
        Conversation conversation = conversationService.getConversationForUser(conversationId, request.senderId());
        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(resolveParticipant(conversation, request.senderId()));
        message.setBody(request.body());
        message.setSentAt(OffsetDateTime.now());
        message.setIsRead(Boolean.FALSE);
        Message saved = messageRepository.save(message);
        return map(saved);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getConversationMessages(UUID conversationId, UUID requesterId) {
        conversationService.getConversationForUser(conversationId, requesterId);
        return messageRepository.findAllByConversation_IdOrderBySentAtAsc(conversationId)
                .stream()
                .map(this::map)
                .toList();
    }

    private void validateParticipant(Rental rental, UUID actorId) {
        UUID lessorId = rental.getLessor() != null ? rental.getLessor().getId() : null;
        UUID lesseeId = rental.getLessee() != null ? rental.getLessee().getId() : null;
        if (!Objects.equals(actorId, lessorId) && !Objects.equals(actorId, lesseeId)) {
            throw new BusinessValidationException("User is not a participant of rental");
        }
    }

    private User resolveUser(Rental rental, UUID userId) {
        if (rental.getLessor() != null && Objects.equals(rental.getLessor().getId(), userId)) {
            return rental.getLessor();
        }
        if (rental.getLessee() != null && Objects.equals(rental.getLessee().getId(), userId)) {
            return rental.getLessee();
        }
        throw new BusinessValidationException("User is not a participant of rental");
    }

    private User resolveParticipant(Conversation conversation, UUID userId) {
        if (conversation.getRental() != null) {
            return resolveUser(conversation.getRental(), userId);
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessValidationException("User not found"));
    }

    private MessageDto map(Message message) {
        return new MessageDto(
                message.getId(),
                message.getSender() != null ? message.getSender().getId() : null,
                message.getBody(),
                message.getSentAt(),
                Boolean.TRUE.equals(message.getIsRead())
        );
    }
}
