package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.is.dto.support.SupportTicketRequest;
import ru.chousik.is.dto.support.SupportTicketResolutionRequest;
import ru.chousik.is.dto.support.SupportTicketResponse;
import ru.chousik.is.dto.support.SupportTicketStatusRequest;
import ru.chousik.is.dto.moderation.FlagRequest;
import ru.chousik.is.entity.Rental;
import ru.chousik.is.entity.SupportTicket;
import ru.chousik.is.entity.SupportTicketStatus;
import ru.chousik.is.entity.User;
import ru.chousik.is.exceptions.BusinessValidationException;
import ru.chousik.is.exceptions.ResourceNotFoundException;
import ru.chousik.is.repository.RentalRepository;
import ru.chousik.is.repository.SupportTicketRepository;
import ru.chousik.is.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportTicketRepository supportTicketRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final NotificationService notificationService;
    private final ModerationService moderationService;

    @Transactional
    public SupportTicketResponse createTicket(SupportTicketRequest request) {
        User requester = userRepository.findById(request.requesterId())
                .orElseThrow(() -> new ResourceNotFoundException("User %s not found".formatted(request.requesterId())));
        Rental rental = null;
        if (request.rentalId() != null) {
            rental = rentalRepository.findById(request.rentalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Rental %s not found".formatted(request.rentalId())));
        }
        SupportTicket ticket = new SupportTicket();
        ticket.setRequester(requester);
        ticket.setRental(rental);
        ticket.setStatus(SupportTicketStatus.OPEN);
        ticket.setSubject(request.subject());
        ticket.setMessage(request.message());
        ticket.setCreatedAt(OffsetDateTime.now());
        SupportTicket saved = supportTicketRepository.save(ticket);
        notificationService.createSystemNotification(requester, "Обращение в поддержку создано: " + request.subject());
        if (rental != null) {
            String reason = buildReason(request.subject(), request.message());
            moderationService.flagRental(rental.getId(), new FlagRequest(request.requesterId(), reason));
        }
        return map(saved);
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getOpenTickets() {
        return supportTicketRepository.findAllByStatus(SupportTicketStatus.OPEN)
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getTickets(String status) {
        if (status == null || status.isBlank()) {
            return supportTicketRepository.findAll().stream().map(this::map).toList();
        }
        SupportTicketStatus parsed;
        try {
            parsed = SupportTicketStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessValidationException("Unknown ticket status: " + status);
        }
        return supportTicketRepository.findAllByStatus(parsed).stream().map(this::map).toList();
    }

    @Transactional
    public SupportTicketResponse startTicket(UUID ticketId, SupportTicketStatusRequest request) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket %s not found".formatted(ticketId)));
        if (ticket.getStatus() == SupportTicketStatus.RESOLVED) {
            throw new BusinessValidationException("Ticket already resolved");
        }
        ticket.setStatus(SupportTicketStatus.IN_PROGRESS);
        SupportTicket saved = supportTicketRepository.save(ticket);
        if (ticket.getRequester() != null) {
            notificationService.createSystemNotification(ticket.getRequester(), "Обращение в поддержку взято в работу.");
        }
        return map(saved);
    }

    @Transactional
    public SupportTicketResponse resolveTicket(UUID ticketId, SupportTicketResolutionRequest request) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket %s not found".formatted(ticketId)));
        ticket.setStatus(SupportTicketStatus.RESOLVED);
        ticket.setResolvedAt(OffsetDateTime.now());
        ticket.setResolutionNotes(request.resolutionNotes());
        SupportTicket saved = supportTicketRepository.save(ticket);
        if (ticket.getRequester() != null) {
            notificationService.createSystemNotification(ticket.getRequester(), "Обращение в поддержку решено.");
        }
        return map(saved);
    }

    private SupportTicketResponse map(SupportTicket ticket) {
        return new SupportTicketResponse(
                ticket.getId(),
                ticket.getRequester() != null ? ticket.getRequester().getId() : null,
                ticket.getRental() != null ? ticket.getRental().getId() : null,
                ticket.getStatus(),
                ticket.getSubject(),
                ticket.getMessage(),
                ticket.getCreatedAt(),
                ticket.getResolvedAt(),
                ticket.getResolutionNotes()
        );
    }

    private String buildReason(String subject, String message) {
        String safeSubject = subject == null ? "" : subject.trim();
        String safeMessage = message == null ? "" : message.trim();
        if (safeSubject.isBlank()) {
            return safeMessage;
        }
        if (safeMessage.isBlank()) {
            return safeSubject;
        }
        return safeSubject + ": " + safeMessage;
    }
}
