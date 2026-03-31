package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.dto.ReportDTO;
import com.kruzvinicius.limsbackend.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for operational and management reports.
 * Restricted to ADMIN and MANAGER roles.
 */
@RestController
@RequestMapping("/api/reports")
@Slf4j
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /** GET /api/reports/dashboard — all indicators aggregated */
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<ReportDTO>> dashboard() {
        log.info("REST request for full dashboard");
        return ResponseEntity.ok(reportService.dashboard());
    }

    /** GET /api/reports/samples-by-status */
    @GetMapping("/samples-by-status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ReportDTO> samplesByStatus() {
        return ResponseEntity.ok(reportService.samplesByStatus());
    }

    /** GET /api/reports/pending-samples */
    @GetMapping("/pending-samples")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST')")
    public ResponseEntity<ReportDTO> pendingSamples() {
        return ResponseEntity.ok(reportService.pendingSamples());
    }

    /** GET /api/reports/rejection-rate */
    @GetMapping("/rejection-rate")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ReportDTO> rejectionRate() {
        return ResponseEntity.ok(reportService.rejectionRate());
    }

    /** GET /api/reports/performance-by-analyst */
    @GetMapping("/performance-by-analyst")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ReportDTO> performanceByAnalyst() {
        return ResponseEntity.ok(reportService.performanceByAnalyst());
    }

    /** GET /api/reports/productivity-by-sector */
    @GetMapping("/productivity-by-sector")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ReportDTO> productivityBySector() {
        return ResponseEntity.ok(reportService.productivityBySector());
    }

    /** GET /api/reports/service-orders-by-status */
    @GetMapping("/service-orders-by-status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ReportDTO> serviceOrdersByStatus() {
        return ResponseEntity.ok(reportService.serviceOrdersByStatus());
    }

    /** GET /api/reports/average-analysis-time */
    @GetMapping("/average-analysis-time")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ReportDTO> averageAnalysisTime() {
        return ResponseEntity.ok(reportService.averageAnalysisTime());
    }

    /** GET /api/reports/nonconformances-by-status */
    @GetMapping("/nonconformances-by-status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ReportDTO> nonConformancesByStatus() {
        return ResponseEntity.ok(reportService.nonConformancesByStatus());
    }
}
