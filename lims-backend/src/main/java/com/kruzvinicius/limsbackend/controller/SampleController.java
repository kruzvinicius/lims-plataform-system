package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.dto.AuditLogDTO;
import com.kruzvinicius.limsbackend.dto.SampleRequest;
import com.kruzvinicius.limsbackend.dto.SampleResponse;
import com.kruzvinicius.limsbackend.dto.TestResultDTO;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Sample management.
 * Provides endpoints for registration, tracking, and analytical results.
 */
@RestController
@RequestMapping("/api/samples")
@Slf4j
@RequiredArgsConstructor
public class SampleController {

    private final SampleService sampleService;

    /**
     * GET /api/samples: Retrieve a paginated list of all samples.
     */
    @GetMapping
    public ResponseEntity<Page<SampleResponse>> getAllSamples(@ParameterObject Pageable pageable) {
        log.info("REST request to get a page of Samples");
        return ResponseEntity.ok(sampleService.findAll(pageable));
    }

    /**
     * GET /api/samples/{id}: Get sample details by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SampleResponse> getSampleById(@PathVariable Long id) {
        log.info("REST request to get Sample : {}", id);
        return ResponseEntity.ok(sampleService.findById(id));
    }

    /**
     * GET /api/samples/customer/{customerId}: Get all samples belonging to a specific customer.
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<SampleResponse>> getSamplesForCustomer(@PathVariable Long customerId) {
        log.info("REST request to get Samples for Customer : {}", customerId);
        return ResponseEntity.ok(sampleService.findByCustomer(customerId));
    }

    /**
     * POST /api/samples: Register a new sample in the system.
     */
    @PostMapping
    public ResponseEntity<SampleResponse> createSample(@Valid @RequestBody SampleRequest sampleRequest) {
        log.info("REST request to save Sample : {}", sampleRequest);
        SampleResponse response = sampleService.create(sampleRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PATCH /api/samples/{id}/status: Update the workflow status of a sample.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<SampleResponse> updateSampleStatus(@PathVariable Long id, @RequestBody String status) {
        log.info("REST request to update Sample {} status to : {}", id, status);
        return ResponseEntity.ok(sampleService.updateStatus(id, status));
    }

    /**
     * GET /api/samples/{id}/history: Retrieve the audit trail (Envers) for a specific sample.
     */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<AuditLogDTO>> getSampleHistory(@PathVariable Long id) {
        log.info("REST request to get Audit History for Sample : {}", id);
        return ResponseEntity.ok(sampleService.getHistory(id));
    }

    /**
     * POST /api/samples/{id}/results: Attach an analytical test result to a sample.
     */
    @PostMapping("/{id}/results")
    public ResponseEntity<TestResultDTO> addTestResult(@PathVariable Long id, @Valid @RequestBody TestResultDTO resultDTO) {
        log.info("REST request to add Test Result for Sample : {}", id);
        return ResponseEntity.status(HttpStatus.CREATED).body(sampleService.addResult(id, resultDTO));
    }

    /**
     * GET /api/samples/{id}/results: List all test results linked to a sample.
     */
    @GetMapping("/{id}/results")
    public ResponseEntity<List<TestResultDTO>> getTestResultsBySampleId(@PathVariable Long id) {
        log.info("REST request to get Test Results for Sample : {}", id);
        return ResponseEntity.ok(sampleService.getResults(id));
    }

    /**
     * GET /api/samples/search?barcode=... Quick lookup by barcode.
     */
    @GetMapping("/search")
    public ResponseEntity<SampleResponse> getSampleByBarcode(@RequestParam String barcode) {
        log.info("REST request to find Sample by Barcode : {}", barcode);
        return sampleService.findByBarcode(barcode)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Sample not found with barcode: " + barcode));
    }
}