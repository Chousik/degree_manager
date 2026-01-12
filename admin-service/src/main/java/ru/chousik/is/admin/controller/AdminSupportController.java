package ru.chousik.is.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import ru.chousik.is.admin.client.UserServiceClient;
import ru.chousik.is.admin.dto.support.SupportTicketResolutionRequest;
import ru.chousik.is.admin.dto.support.SupportTicketResponse;
import ru.chousik.is.admin.dto.support.SupportTicketStatusRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/support")
@RequiredArgsConstructor
public class AdminSupportController {

    private final UserServiceClient userServiceClient;

    @GetMapping("/tickets/open")
    public List<SupportTicketResponse> getOpenTickets(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return userServiceClient.getOpenTickets(authHeader);
    }

    @GetMapping("/tickets")
    public List<SupportTicketResponse> getTickets(@RequestParam(value = "status", required = false) String status,
                                                  @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        return userServiceClient.getTickets(status, authHeader);
    }

    @PostMapping("/tickets/{ticketId}/start")
    public SupportTicketResponse startTicket(@PathVariable UUID ticketId,
                                             @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                             @Valid @RequestBody SupportTicketStatusRequest request) {
        return userServiceClient.startTicket(ticketId, request, authHeader);
    }

    @PostMapping("/tickets/{ticketId}/resolve")
    public SupportTicketResponse resolveTicket(@PathVariable UUID ticketId,
                                               @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                               @Valid @RequestBody SupportTicketResolutionRequest request) {
        return userServiceClient.resolveTicket(ticketId, request, authHeader);
    }
}
