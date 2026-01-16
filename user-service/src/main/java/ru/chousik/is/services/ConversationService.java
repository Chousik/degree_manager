package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.chousik.is.entity.*;
import ru.chousik.is.exceptions.BusinessValidationException;
import ru.chousik.is.exceptions.ResourceNotFoundException;
import ru.chousik.is.repository.ConversationPairRepository;
import ru.chousik.is.repository.ConversationRepository;
import ru.chousik.is.repository.ListingPhotoRepository;
import ru.chousik.is.repository.ListingRepository;
import ru.chousik.is.repository.MessageRepository;
import ru.chousik.is.repository.UserRepository;
import ru.chousik.is.dto.conversation.ConversationSummaryResponse;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final ConversationPairRepository conversationPairRepository;
    private final ListingRepository listingRepository;
    private final UserRepository userRepository;
    private final ListingPhotoRepository listingPhotoRepository;
    private final MessageRepository messageRepository;

    @Transactional
    public Conversation ensureConversation(Rental rental) {
        Conversation conversation = conversationRepository.findByRental_Id(rental.getId())
                .orElseGet(() -> {
                    Conversation created = new Conversation();
                    created.setRental(rental);
                    created.setListing(rental.getListing());
                    created.setCreatedAt(OffsetDateTime.now());
                    return conversationRepository.save(created);
                });
        if (conversation.getListing() == null && rental.getListing() != null) {
            conversation.setListing(rental.getListing());
        }
        addParticipant(conversation, rental.getLessor());
        addParticipant(conversation, rental.getLessee());
        return conversation;
    }

    public Conversation getConversation(Rental rental) {
        return conversationRepository.findByRental_Id(rental.getId())
                .orElseThrow(() -> new IllegalStateException("Conversation for rental %s not found".formatted(rental.getId())));
    }

    @Transactional
    public Conversation ensureListingConversation(UUID listingId, UUID requesterId) {
        Listing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing %s not found".formatted(listingId)));
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("User %s not found".formatted(requesterId)));
        if (listing.getOwner() != null && Objects.equals(listing.getOwner().getId(), requesterId)) {
            throw new BusinessValidationException("Нельзя начать чат с самим собой");
        }

        Conversation conversation = conversationRepository.findByListingAndParticipant(listingId, requesterId)
                .orElseGet(() -> {
                    Conversation created = new Conversation();
                    created.setListing(listing);
                    created.setCreatedAt(OffsetDateTime.now());
                    return conversationRepository.save(created);
                });
        addParticipant(conversation, listing.getOwner());
        addParticipant(conversation, requester);
        return conversation;
    }

    @Transactional(readOnly = true)
    public Conversation getConversationForUser(UUID conversationId, UUID userId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation %s not found".formatted(conversationId)));
        if (!isParticipant(conversationId, userId)) {
            throw new BusinessValidationException("User is not a participant of conversation");
        }
        return conversation;
    }

    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> getUserConversations(UUID userId) {
        List<Conversation> conversations = conversationRepository.findAllByParticipant(userId);
        List<UUID> listingIds = conversations.stream()
                .map(this::resolveListing)
                .filter(Objects::nonNull)
                .map(Listing::getId)
                .distinct()
                .toList();

        Map<UUID, String> listingPhotos = listingIds.isEmpty()
                ? Map.of()
                : listingPhotoRepository.findByListing_IdIn(listingIds).stream()
                .collect(Collectors.groupingBy(photo -> photo.getListing().getId()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .min(Comparator.comparing(ListingPhoto::getSortOrder, Comparator.nullsLast(Short::compareTo)))
                                .map(ListingPhoto::getUrl)
                                .orElse(null)
                ));

        return conversations.stream()
                .map(conversation -> mapSummary(conversation, userId, listingPhotos))
                .toList();
    }

    private boolean isParticipant(UUID conversationId, UUID userId) {
        ConversationPairId id = new ConversationPairId();
        id.setConversationId(conversationId);
        id.setUserId(userId);
        return conversationPairRepository.existsById(id);
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

    private Listing resolveListing(Conversation conversation) {
        if (conversation.getListing() != null) {
            return conversation.getListing();
        }
        if (conversation.getRental() != null) {
            return conversation.getRental().getListing();
        }
        return null;
    }

    private ConversationSummaryResponse mapSummary(Conversation conversation, UUID userId, Map<UUID, String> listingPhotos) {
        Listing listing = resolveListing(conversation);
        UUID listingId = listing != null ? listing.getId() : null;
        String listingTitle = listing != null ? listing.getTitle() : null;
        String listingPhoto = listingId != null ? listingPhotos.get(listingId) : null;

        User counterparty = conversationPairRepository.findOtherParticipants(conversation.getId(), userId)
                .stream()
                .findFirst()
                .orElse(null);
        String counterpartyName = formatName(counterparty);
        String counterpartyUsername = counterparty != null ? counterparty.getUsername() : null;

        Message lastMessage = messageRepository.findTopByConversation_IdOrderBySentAtDesc(conversation.getId());
        String preview = lastMessage != null ? lastMessage.getBody() : null;
        OffsetDateTime lastAt = lastMessage != null ? lastMessage.getSentAt() : null;

        return new ConversationSummaryResponse(
                conversation.getId(),
                listingId,
                listingTitle,
                listingPhoto,
                counterparty != null ? counterparty.getId() : null,
                counterpartyName,
                counterpartyUsername,
                preview,
                lastAt
        );
    }

    private String formatName(User user) {
        if (user == null) {
            return "Пользователь";
        }
        String first = user.getName();
        String last = user.getSurname();
        if (StringUtils.hasText(first) && StringUtils.hasText(last)) {
            return first + " " + last;
        }
        if (StringUtils.hasText(first)) {
            return first;
        }
        if (StringUtils.hasText(last)) {
            return last;
        }
        return user.getUsername() != null ? user.getUsername() : "Пользователь";
    }
}
