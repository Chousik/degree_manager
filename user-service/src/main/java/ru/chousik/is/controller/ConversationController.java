package ru.chousik.is.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.chousik.is.dto.conversation.ConversationResponse;
import ru.chousik.is.dto.conversation.ConversationStartRequest;
import ru.chousik.is.dto.conversation.ConversationSummaryResponse;
import ru.chousik.is.dto.message.MessageDto;
import ru.chousik.is.dto.message.MessageRequest;
import ru.chousik.is.services.ConversationService;
import ru.chousik.is.services.MessagingService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;
    private final MessagingService messagingService;

    @PostMapping("/listings/{listingId}")
    public ConversationResponse startListingConversation(@PathVariable UUID listingId,
                                                         @Valid @RequestBody ConversationStartRequest request) {
        var conversation = conversationService.ensureListingConversation(listingId, request.senderId());
        messagingService.sendConversationMessage(conversation.getId(), new MessageRequest(request.senderId(), request.body()));
        return new ConversationResponse(conversation.getId(), listingId);
    }

    @GetMapping
    public List<ConversationSummaryResponse> getConversations(@RequestParam UUID userId) {
        return conversationService.getUserConversations(userId);
    }

    @GetMapping("/{conversationId}/messages")
    public List<MessageDto> getMessages(@PathVariable UUID conversationId, @RequestParam UUID userId) {
        return messagingService.getConversationMessages(conversationId, userId);
    }

    @PostMapping("/{conversationId}/messages")
    public MessageDto sendMessage(@PathVariable UUID conversationId, @Valid @RequestBody MessageRequest request) {
        return messagingService.sendConversationMessage(conversationId, request);
    }
}
