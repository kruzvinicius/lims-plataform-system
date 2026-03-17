package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.dto.AuditLogDTO;
import com.kruzvinicius.limsbackend.dto.SampleDTO;
import com.kruzvinicius.limsbackend.dto.TestResultDTO;
import com.kruzvinicius.limsbackend.model.Sample;
import com.kruzvinicius.limsbackend.model.TestResult;
import com.kruzvinicius.limsbackend.service.SampleService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/samples")
@Slf4j
@RequiredArgsConstructor
public class SampleController {

    private final SampleService sampleService;

    @GetMapping
    public ResponseEntity<Page<SampleDTO>> getAllSamples(@ParameterObject Pageable pageable) {
        return ResponseEntity.ok(sampleService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SampleDTO> getSampleById(@PathVariable Long id) {
        return ResponseEntity.ok(sampleService.findById(id));
    }

    @GetMapping("/customer/{customerId}")
    public List<SampleDTO> getSamplesForCustomer(@PathVariable Long customerId) {
        return sampleService.findByCustomer(customerId);
    }

    @PostMapping
    public ResponseEntity<SampleDTO> createSample(@Valid @RequestBody SampleDTO sampleDTO) {
        log.info("Received sample for creation: {}", sampleDTO);
        return ResponseEntity.ok(sampleService.create(sampleDTO));
    }

    @PatchMapping("/{id}/status")
    public Sample updateSampleStatus(@PathVariable Long id, @RequestBody String status) {
        return sampleService.updateStatus(id, status);
    }

    @GetMapping("/{id}/history")
    public List<AuditLogDTO> getSampleHistory(@PathVariable Long id) {
        return sampleService.getHistory(id);
    }

    @PostMapping("/{id}/results")
    public ResponseEntity<TestResult> addTestResult(@PathVariable Long id, @RequestBody TestResult result) {
        return ResponseEntity.ok(sampleService.addResult(id, result));
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<List<TestResultDTO>> getTestResultsBySampleId(@PathVariable Long id) {
        return ResponseEntity.ok(sampleService.getResults(id));
    }

    @GetMapping("/search")
    public ResponseEntity<SampleDTO> getSampleByBarcode(@RequestParam String barcode) {
        return sampleService.findByBarcode(barcode)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new EntityNotFoundException("Sample not found with barcode: " + barcode));
    }
}