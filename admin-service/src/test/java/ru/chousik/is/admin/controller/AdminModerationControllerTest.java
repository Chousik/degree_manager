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
import ru.chousik.is.admin.dto.moderation.FlaggedListingDto;
import ru.chousik.is.admin.dto.moderation.ModerationResolutionRequest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminModerationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminModerationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceClient userServiceClient;

    @Test
    void getFlaggedListings_returnsList() throws Exception {
        FlaggedListingDto listing = new FlaggedListingDto(
                UUID.randomUUID(),
                "Item",
                new BigDecimal("10.00"),
                "Reason",
                OffsetDateTime.now()
        );
        when(userServiceClient.getFlaggedListings("Bearer token")).thenReturn(List.of(listing));

        mockMvc.perform(get("/api/admin/moderation/listings")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$", hasSize(1)));
    }

    @Test
    void resolveListing_returnsNoContent() throws Exception {
        UUID listingId = UUID.randomUUID();
        ModerationResolutionRequest request = new ModerationResolutionRequest(
                UUID.randomUUID(),
                "HIDE",
                "ok"
        );

        mockMvc.perform(post("/api/admin/moderation/listings/{listingId}/resolve", listingId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isNoContent());

        verify(userServiceClient).resolveListing(listingId, request, "Bearer token");
    }
}
