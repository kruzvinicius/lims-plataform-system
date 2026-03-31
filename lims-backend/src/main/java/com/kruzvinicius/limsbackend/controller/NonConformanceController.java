package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.dto.NonConformanceDTO;
import com.kruzvinicius.limsbackend.model.enums.NonConformanceSeverity;
import com.kruzvinicius.limsbackend.model.enums.NonConformanceStatus;
import com.kruzvinicius.limsbackend.service.NonConformanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Non-Conformance management.
 * Full QC lifecycle: OPEN → UNDER_INVESTIGATION → RESOLVED → CLOSED.
 */
@RestController
@RequestMapping("/api/nonconformances")
@Slf4j
@RequiredArgsConstructor
public class NonConformanceController {

    private final NonConformanceService ncService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<NonConformanceDTO> create(@Valid @RequestBody NonConformanceDTO dto) {
        log.info("REST request to create NonConformance: {}", dto.title());
        return ResponseEntity.status(HttpStatus.CREATED).body(ncService.create(dto));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<NonConformanceDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ncService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST')")
    public ResponseEntity<List<NonConformanceDTO>> getAll(
            @RequestParam(required = false) NonConformanceStatus status,
            @RequestParam(required = false) NonConformanceSeverity severity) {
        return ResponseEntity.ok(ncService.findAll(status, severity));
    }

    @GetMapping("/sample/{sampleId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST')")
    public ResponseEntity<List<NonConformanceDTO>> getBySample(@PathVariable Long sampleId) {
        return ResponseEntity.ok(ncService.findBySample(sampleId));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<NonConformanceDTO> assign(
            @PathVariable Long id, @RequestParam String username) {
        log.info("REST request to assign NC {} to {}", id, username);
        return ResponseEntity.ok(ncService.assign(id, username));
    }

    @PatchMapping("/{id}/investigate")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST')")
    public ResponseEntity<NonConformanceDTO> investigate(
            @PathVariable Long id, @RequestBody String rootCause) {
        return ResponseEntity.ok(ncService.investigate(id, rootCause));
    }

    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<NonConformanceDTO> resolve(
            @PathVariable Long id, @RequestBody NonConformanceDTO dto) {
        return ResponseEntity.ok(ncService.resolve(id, dto.correctiveAction(), dto.preventiveAction()));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<NonConformanceDTO> close(@PathVariable Long id) {
        return ResponseEntity.ok(ncService.close(id));
    }
}
