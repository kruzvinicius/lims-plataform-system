package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.dto.ApprovalRequest;
import com.kruzvinicius.limsbackend.dto.AuditLogDTO;
import com.kruzvinicius.limsbackend.dto.TestResultDTO;
import com.kruzvinicius.limsbackend.service.AuditService;
import com.kruzvinicius.limsbackend.service.TestResultService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for individual test result approval workflow and version history.
 */
@RestController
@RequestMapping("/api/results")
@Slf4j
@RequiredArgsConstructor
public class TestResultController {

    private final TestResultService testResultService;
    private final AuditService auditService;

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<TestResultDTO> approveResult(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest request) {
        log.info("REST request to approve Test Result : {}", id);
        return ResponseEntity.ok(testResultService.approveResult(id, request));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<TestResultDTO> rejectResult(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest request) {
        log.info("REST request to reject Test Result : {}", id);
        return ResponseEntity.ok(testResultService.rejectResult(id, request));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<AuditLogDTO>> getResultHistory(@PathVariable Long id) {
        log.info("REST request for version history of Test Result : {}", id);
        return ResponseEntity.ok(auditService.getResultHistory(id));
    }
}
