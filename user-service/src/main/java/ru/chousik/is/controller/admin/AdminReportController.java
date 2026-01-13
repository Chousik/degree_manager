package ru.chousik.is.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.chousik.is.dto.admin.report.ReportResolutionRequest;
import ru.chousik.is.dto.admin.report.ReportSummaryDto;
import ru.chousik.is.services.admin.AdminReportService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping
    public List<ReportSummaryDto> getReports(@RequestParam(value = "status", required = false) String status) {
        return adminReportService.getReports(status);
    }

    @PostMapping("/{reportId}/resolve")
    public ResponseEntity<ReportSummaryDto> resolveReport(@PathVariable UUID reportId,
                                                          @Valid @RequestBody ReportResolutionRequest request) {
        return ResponseEntity.ok(adminReportService.resolveReport(reportId, request));
    }
}
