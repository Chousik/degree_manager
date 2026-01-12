package ru.chousik.is.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chousik.is.dto.support.SupportTicketRequest;
import ru.chousik.is.dto.support.SupportTicketResolutionRequest;
import ru.chousik.is.dto.support.SupportTicketResponse;
import ru.chousik.is.dto.support.SupportTicketStatusRequest;
import ru.chousik.is.services.SupportService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;

    @PostMapping("/tickets")
    public ResponseEntity<SupportTicketResponse> createTicket(@Valid @RequestBody SupportTicketRequest request) {
        SupportTicketResponse response = supportService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/tickets/open")
    public List<SupportTicketResponse> getOpenTickets() {
        return supportService.getOpenTickets();
    }

    @GetMapping("/tickets")
    public List<SupportTicketResponse> getTickets(@RequestParam(value = "status", required = false) String status) {
        return supportService.getTickets(status);
    }

    @PostMapping("/tickets/{ticketId}/start")
    public SupportTicketResponse startTicket(@PathVariable UUID ticketId,
                                             @Valid @RequestBody SupportTicketStatusRequest request) {
        return supportService.startTicket(ticketId, request);
    }

    @PostMapping("/tickets/{ticketId}/resolve")
    public SupportTicketResponse resolve(@PathVariable UUID ticketId,
                                         @Valid @RequestBody SupportTicketResolutionRequest request) {
        return supportService.resolveTicket(ticketId, request);
    }
}
