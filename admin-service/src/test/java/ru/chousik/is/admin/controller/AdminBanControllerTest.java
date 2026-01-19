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
import ru.chousik.is.admin.dto.ban.BanCreateRequest;
import ru.chousik.is.admin.dto.ban.BanResponse;

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

@WebMvcTest(AdminBanController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminBanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceClient userServiceClient;

    @Test
    void getBans_returnsList() throws Exception {
        BanResponse response = new BanResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Reason",
                "TEMP",
                OffsetDateTime.now(),
                "OPEN",
                OffsetDateTime.now()
        );
        when(userServiceClient.getBans("OPEN", "Bearer token")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/admin/bans")
                        .param("status", "OPEN")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void createBan_returnsResponse() throws Exception {
        UUID adminId = UUID.randomUUID();
        BanCreateRequest request = new BanCreateRequest(
                adminId,
                UUID.randomUUID(),
                "reason",
                "TEMP",
                OffsetDateTime.now().plusDays(1),
                "OPEN"
        );
        BanResponse response = new BanResponse(
                UUID.randomUUID(),
                request.bannedUserId(),
                adminId,
                request.banReason(),
                request.banType(),
                request.banDuration(),
                request.status(),
                OffsetDateTime.now()
        );
        when(userServiceClient.createBan(request, "Bearer token")).thenReturn(response);

        mockMvc.perform(post("/api/admin/bans")
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(userServiceClient).createBan(request, "Bearer token");
    }
}
