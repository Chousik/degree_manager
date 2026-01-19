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
import ru.chousik.is.admin.dto.listing.ListingArchiveRequest;
import ru.chousik.is.admin.dto.listing.ListingDetailDto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminListingController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminListingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceClient userServiceClient;

    @Test
    void getListing_returnsDetail() throws Exception {
        UUID listingId = UUID.randomUUID();
        ListingDetailDto detail = new ListingDetailDto(
                listingId,
                UUID.randomUUID(),
                "Title",
                "Desc",
                new BigDecimal("10.00"),
                null,
                false,
                "AVAILABLE",
                null,
                null,
                OffsetDateTime.now(),
                List.of(),
                List.of(),
                List.of(),
                false,
                null
        );
        when(userServiceClient.getListingDetail(listingId, "Bearer token")).thenReturn(detail);

        mockMvc.perform(get("/api/admin/listings/{listingId}", listingId)
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    void archiveListing_returnsNoContent() throws Exception {
        UUID listingId = UUID.randomUUID();
        ListingArchiveRequest request = new ListingArchiveRequest(UUID.randomUUID(), "Outdated");

        mockMvc.perform(post("/api/admin/listings/{listingId}/archive", listingId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isNoContent());

        verify(userServiceClient).archiveListing(listingId, request, "Bearer token");
    }
}
