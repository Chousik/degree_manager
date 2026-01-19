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
import ru.chousik.is.admin.dto.report.ReportResolutionRequest;
import ru.chousik.is.admin.dto.report.ReportSummaryDto;

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

@WebMvcTest(AdminReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServiceClient userServiceClient;

    @Test
    void getReports_returnsList() throws Exception {
        UUID reportId = UUID.randomUUID();
        ReportSummaryDto report = new ReportSummaryDto(
                reportId,
                UUID.randomUUID(),
                "OPEN",
                "LISTING",
                UUID.randomUUID(),
                "Reason",
                null,
                OffsetDateTime.now(),
                null
        );
        when(userServiceClient.getReports(null, "Bearer token"))
                .thenReturn(List.of(report));

        mockMvc.perform(get("/api/admin/reports")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(reportId.toString()));
    }

    @Test
    void resolveReport_returnsResolvedReport() throws Exception {
        UUID reportId = UUID.randomUUID();
        ReportResolutionRequest request = new ReportResolutionRequest(
                UUID.randomUUID(),
                "RESOLVED",
                "ok"
        );
        ReportSummaryDto report = new ReportSummaryDto(
                reportId,
                request.adminId(),
                "RESOLVED",
                "LISTING",
                UUID.randomUUID(),
                "Reason",
                "ok",
                OffsetDateTime.now(),
                request.adminId()
        );
        when(userServiceClient.resolveReport(reportId, request, "Bearer token")).thenReturn(report);

        mockMvc.perform(post("/api/admin/reports/{id}/resolve", reportId)
                        .header("Authorization", "Bearer token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"));

        verify(userServiceClient).resolveReport(reportId, request, "Bearer token");
    }
}
