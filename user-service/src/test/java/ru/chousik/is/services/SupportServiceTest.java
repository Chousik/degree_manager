package ru.chousik.is.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.chousik.is.dto.moderation.FlagRequest;
import ru.chousik.is.dto.support.SupportTicketRequest;
import ru.chousik.is.dto.support.SupportTicketResolutionRequest;
import ru.chousik.is.entity.Rental;
import ru.chousik.is.entity.SupportTicket;
import ru.chousik.is.entity.SupportTicketStatus;
import ru.chousik.is.entity.User;
import ru.chousik.is.exceptions.BusinessValidationException;
import ru.chousik.is.repository.RentalRepository;
import ru.chousik.is.repository.SupportTicketRepository;
import ru.chousik.is.repository.UserRepository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportServiceTest {

    @Mock
    private SupportTicketRepository supportTicketRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private ModerationService moderationService;

    @InjectMocks
    private SupportService supportService;

    @Test
    void createTicket_withRentalFlagsAndNotifies() {
        UUID userId = UUID.randomUUID();
        UUID rentalId = UUID.randomUUID();
        User user = new User();
        user.setId(userId);
        Rental rental = new Rental();
        rental.setId(rentalId);

        SupportTicket saved = new SupportTicket();
        saved.setId(UUID.randomUUID());
        saved.setRequester(user);
        saved.setRental(rental);
        saved.setStatus(SupportTicketStatus.OPEN);
        saved.setSubject("Возврат");
        saved.setMessage("Проблема с возвратом");
        saved.setCreatedAt(OffsetDateTime.now());

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(rentalRepository.findById(rentalId)).thenReturn(Optional.of(rental));
        when(supportTicketRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(saved);

        SupportTicketRequest request = new SupportTicketRequest(userId, rentalId, "Возврат", "Проблема с возвратом");
        var response = supportService.createTicket(request);

        assertThat(response.status()).isEqualTo(SupportTicketStatus.OPEN);
        verify(notificationService).createSystemNotification(user, "Обращение в поддержку создано: Возврат");

        ArgumentCaptor<FlagRequest> captor = ArgumentCaptor.forClass(FlagRequest.class);
        verify(moderationService).flagRental(org.mockito.ArgumentMatchers.eq(rentalId), captor.capture());
        assertThat(captor.getValue().reason()).isEqualTo("Возврат: Проблема с возвратом");
    }

    @Test
    void getTickets_invalidStatusThrows() {
        assertThatThrownBy(() -> supportService.getTickets("unknown"))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void startTicket_resolvedTicketThrows() {
        UUID ticketId = UUID.randomUUID();
        SupportTicket ticket = new SupportTicket();
        ticket.setId(ticketId);
        ticket.setStatus(SupportTicketStatus.RESOLVED);
        when(supportTicketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> supportService.startTicket(ticketId, null))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void resolveTicket_updatesStatus() {
        UUID ticketId = UUID.randomUUID();
        User requester = new User();
        requester.setId(UUID.randomUUID());
        SupportTicket ticket = new SupportTicket();
        ticket.setId(ticketId);
        ticket.setRequester(requester);
        when(supportTicketRepository.findById(ticketId)).thenReturn(Optional.of(ticket));
        when(supportTicketRepository.save(ticket)).thenReturn(ticket);

        var response = supportService.resolveTicket(ticketId,
                new SupportTicketResolutionRequest(UUID.randomUUID(), "Done"));

        assertThat(response.status()).isEqualTo(SupportTicketStatus.RESOLVED);
        verify(notificationService).createSystemNotification(requester, "Обращение в поддержку решено.");
    }
}
