package ru.chousik.is.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.chousik.is.dto.support.SupportTicketRequest;
import ru.chousik.is.dto.support.SupportTicketResolutionRequest;
import ru.chousik.is.dto.support.SupportTicketResponse;
import ru.chousik.is.entity.Rental;
import ru.chousik.is.entity.SupportTicket;
import ru.chousik.is.entity.SupportTicketStatus;
import ru.chousik.is.entity.User;
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
        return map(saved);
    }

    @Transactional(readOnly = true)
    public List<SupportTicketResponse> getOpenTickets() {
        return supportTicketRepository.findAllByStatus(SupportTicketStatus.OPEN)
                .stream()
                .map(this::map)
                .toList();
    }

    @Transactional
    public SupportTicketResponse resolveTicket(UUID ticketId, SupportTicketResolutionRequest request) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket %s not found".formatted(ticketId)));
        ticket.setStatus(SupportTicketStatus.RESOLVED);
        ticket.setResolvedAt(OffsetDateTime.now());
        ticket.setResolutionNotes(request.resolutionNotes());
        SupportTicket saved = supportTicketRepository.save(ticket);
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
}
