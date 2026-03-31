package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.dto.AuditLogDTO;
import com.kruzvinicius.limsbackend.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for audit trail queries.
 * Read-only endpoints to inspect change history.
 */
@RestController
@RequestMapping("/api/audit")
@Slf4j
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/samples/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<AuditLogDTO>> getSampleHistory(@PathVariable Long id) {
        log.info("REST request for audit history of Sample : {}", id);
        return ResponseEntity.ok(auditService.getSampleHistory(id));
    }

    @GetMapping("/results/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<AuditLogDTO>> getResultHistory(@PathVariable Long id) {
        log.info("REST request for audit history of TestResult : {}", id);
        return ResponseEntity.ok(auditService.getResultHistory(id));
    }
}
