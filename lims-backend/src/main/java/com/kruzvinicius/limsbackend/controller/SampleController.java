package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.dto.*;
import com.kruzvinicius.limsbackend.service.SampleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Sample management.
 * Covers: registration, tracking, analytical results, approvals, rejections and reanalysis.
 */
@RestController
@RequestMapping("/api/samples")
@Slf4j
@RequiredArgsConstructor
public class SampleController {

    private final SampleService sampleService;

    // ── READ ──────────────────────────────────────────────────────────────────

    /** GET /api/samples — paginated list of all samples */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<Page<SampleResponse>> getAllSamples(@ParameterObject Pageable pageable) {
        log.info("REST request to get a page of Samples");
        return ResponseEntity.ok(sampleService.findAll(pageable));
    }

    /** GET /api/samples/{id} — sample by ID */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN','CLIENT')")
    public ResponseEntity<SampleResponse> getSampleById(@PathVariable Long id) {
        log.info("REST request to get Sample : {}", id);
        return ResponseEntity.ok(sampleService.findById(id));
    }

    /** GET /api/samples/customer/{customerId} — all samples for a customer */
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN','CLIENT')")
    public ResponseEntity<List<SampleResponse>> getSamplesForCustomer(@PathVariable Long customerId) {
        log.info("REST request to get Samples for Customer : {}", customerId);
        return ResponseEntity.ok(sampleService.findByCustomer(customerId));
    }

    /** GET /api/samples/search?barcode=... — quick lookup by barcode */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<SampleResponse> getSampleByBarcode(@RequestParam String barcode) {
        log.info("REST request to find Sample by Barcode : {}", barcode);
        return sampleService.findByBarcode(barcode)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Sample not found with barcode: " + barcode));
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    /** POST /api/samples — register a new sample */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<SampleResponse> createSample(@Valid @RequestBody SampleRequest sampleRequest) {
        log.info("REST request to save Sample : {}", sampleRequest);
        SampleResponse response = sampleService.create(sampleRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ── STATUS & WORKFLOW ─────────────────────────────────────────────────────

    /** PATCH /api/samples/{id}/status — generic status transition */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST')")
    public ResponseEntity<SampleResponse> updateSampleStatus(@PathVariable Long id, @RequestBody String status) {
        log.info("REST request to update Sample {} status to : {}", id, status);
        return ResponseEntity.ok(sampleService.updateStatus(id, status));
    }

    /** POST /api/samples/{id}/approve — supervisor approves sample results */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<SampleResponse> approveSample(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest request) {
        log.info("REST request to approve Sample : {}", id);
        return ResponseEntity.ok(sampleService.approve(id, request));
    }

    /** POST /api/samples/{id}/release — release sample to customer */
    @PostMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<SampleResponse> releaseSample(@PathVariable Long id) {
        log.info("REST request to release Sample : {}", id);
        return ResponseEntity.ok(sampleService.release(id));
    }

    /** POST /api/samples/{id}/reject — reject sample with reason */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<SampleResponse> rejectSample(
            @PathVariable Long id,
            @Valid @RequestBody ApprovalRequest request) {
        log.info("REST request to reject Sample : {}", id);
        return ResponseEntity.ok(sampleService.reject(id, request));
    }

    // ── REANALYSIS ─────────────────────────────────────────────────────────────

    /** POST /api/samples/{id}/reanalysis — request reanalysis after rejection */
    @PostMapping("/{id}/reanalysis")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST')")
    public ResponseEntity<ReanalysisRequestDTO> requestReanalysis(
            @PathVariable Long id,
            @Valid @RequestBody ReanalysisRequestDTO dto) {
        log.info("REST request to request Reanalysis for Sample : {}", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(sampleService.requestReanalysis(id, dto));
    }

    /** GET /api/samples/{id}/reanalysis — list reanalysis history for sample */
    @GetMapping("/{id}/reanalysis")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST')")
    public ResponseEntity<List<ReanalysisRequestDTO>> getReanalysisHistory(@PathVariable Long id) {
        log.info("REST request to get Reanalysis history for Sample : {}", id);
        return ResponseEntity.ok(sampleService.getReanalysisHistory(id));
    }

    // ── TEST RESULTS ──────────────────────────────────────────────────────────

    /** POST /api/samples/{id}/results — attach analytical result to sample */
    @PostMapping("/{id}/results")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST')")
    public ResponseEntity<TestResultDTO> addTestResult(
            @PathVariable Long id,
            @Valid @RequestBody TestResultDTO resultDTO) {
        log.info("REST request to add Test Result for Sample : {}", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(sampleService.addResult(id, resultDTO));
    }

    /** GET /api/samples/{id}/results — list all test results for sample */
    @GetMapping("/{id}/results")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN','CLIENT')")
    public ResponseEntity<List<TestResultDTO>> getTestResultsBySampleId(@PathVariable Long id) {
        log.info("REST request to get Test Results for Sample : {}", id);
        return ResponseEntity.ok(sampleService.getResults(id));
    }

    // ── AUDIT ─────────────────────────────────────────────────────────────────

    /** GET /api/samples/{id}/history — Envers audit trail */
    @GetMapping("/{id}/history")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<AuditLogDTO>> getSampleHistory(@PathVariable Long id) {
        log.info("REST request to get Audit History for Sample : {}", id);
        return ResponseEntity.ok(sampleService.getHistory(id));
    }
}