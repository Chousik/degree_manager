package ru.chousik.is.admin.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chousik.is.admin.client.UserServiceClient;
import ru.chousik.is.admin.dto.report.ReportResolutionRequest;
import ru.chousik.is.admin.dto.report.ReportSummaryDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final UserServiceClient userServiceClient;

    @GetMapping
    public List<ReportSummaryDto> getReports(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                             @RequestParam(value = "status", required = false) String status) {
        return userServiceClient.getReports(status, authHeader);
    }

    @PostMapping("/{reportId}/resolve")
    public ResponseEntity<ReportSummaryDto> resolveReport(@PathVariable UUID reportId,
                                                          @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                                          @Valid @RequestBody ReportResolutionRequest request) {
        return ResponseEntity.ok(userServiceClient.resolveReport(reportId, request, authHeader));
    }
}
