package ru.chousik.is.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.chousik.is.admin.client.UserServiceClient;
import ru.chousik.is.admin.dto.support.SupportTicketResolutionRequest;
import ru.chousik.is.admin.dto.support.SupportTicketResponse;
import ru.chousik.is.admin.dto.support.SupportTicketStatusRequest;
import ru.chousik.is.admin.model.SupportTicketStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminSupportController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminSupportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceClient userServiceClient;

    @Test
    void getTickets_returnsList() throws Exception {
        SupportTicketResponse ticket = new SupportTicketResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                null,
                SupportTicketStatus.OPEN,
                "Subject",
                "Message",
                OffsetDateTime.now(),
                null,
                null
        );
        when(userServiceClient.getTickets("OPEN", "Bearer token")).thenReturn(List.of(ticket));

        mockMvc.perform(get("/api/admin/support/tickets")
                        .param("status", "OPEN")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void startTicket_returnsTicket() throws Exception {
        UUID ticketId = UUID.randomUUID();
        SupportTicketStatusRequest request = new SupportTicketStatusRequest(UUID.randomUUID());
        SupportTicketResponse response = new SupportTicketResponse(
                ticketId,
                UUID.randomUUID(),
                null,
                SupportTicketStatus.IN_PROGRESS,
                "Subject",
                "Message",
                OffsetDateTime.now(),
                null,
                null
        );
        when(userServiceClient.startTicket(ticketId, request, "Bearer token")).thenReturn(response);

        mockMvc.perform(post("/api/admin/support/tickets/{ticketId}/start", ticketId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        verify(userServiceClient).startTicket(ticketId, request, "Bearer token");
    }

    @Test
    void resolveTicket_returnsTicket() throws Exception {
        UUID ticketId = UUID.randomUUID();
        SupportTicketResolutionRequest request = new SupportTicketResolutionRequest(UUID.randomUUID(), "Done");
        SupportTicketResponse response = new SupportTicketResponse(
                ticketId,
                UUID.randomUUID(),
                null,
                SupportTicketStatus.RESOLVED,
                "Subject",
                "Message",
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "Done"
        );
        when(userServiceClient.resolveTicket(ticketId, request, "Bearer token")).thenReturn(response);

        mockMvc.perform(post("/api/admin/support/tickets/{ticketId}/resolve", ticketId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        verify(userServiceClient).resolveTicket(ticketId, request, "Bearer token");
    }
}
