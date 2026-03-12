package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.dto.AuditLogDTO;
import com.kruzvinicius.limsbackend.dto.SampleDTO;
import com.kruzvinicius.limsbackend.dto.TestResultDTO;
import com.kruzvinicius.limsbackend.model.Sample;
import com.kruzvinicius.limsbackend.model.TestResult;
import com.kruzvinicius.limsbackend.repository.SampleRepository;
import com.kruzvinicius.limsbackend.repository.TestResultRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/samples")
@Slf4j
@RequiredArgsConstructor
public class SampleController {

    private final SampleRepository sampleRepository;
    private final EntityManager entityManager;
    private final TestResultRepository testResultRepository;

    // Get all samples
    @GetMapping
    public List<SampleDTO> getAllSamples() {
        return sampleRepository.findAll().stream()
                .map(this::mapSampleToDTO)
                .toList();
    }

    @GetMapping("/customer/{customerId}")
    public List<SampleDTO> getSamplesForCustomer(@PathVariable Long customerId) {
        log.info("Getting samples for customer with ID: {}", customerId);
        return sampleRepository.findByCustomerId(customerId).stream()
                .map(this::mapSampleToDTO)
                .toList();
    }

    // (POST)
    @PostMapping
    public SampleDTO createSample(@RequestBody Sample sample) {
        log.info("Sample received for creation: {}", sample);
        return mapSampleToDTO(sampleRepository.save(sample));
    }

    // (Patch)
    @PatchMapping("/{id}/status")
    public Sample updateSampleStatus(@PathVariable Long id, @RequestBody String status) {
        // Find the sample by ID
        Sample sample = sampleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sample not found with id: " + id));

        // Use the 'status' variable received in the parameter
        sample.setStatus(status);

        log.info("Updating sample {} to status: {}", id, status);

        // Save triggers the Hibernate Envers audit records
        return sampleRepository.save(sample);
    }

    @GetMapping("/{id}/history")
    public List<AuditLogDTO> getSampleHistory(@PathVariable Long id) {
        log.info("Fetching history for sample with ID: {}", id);

        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        @SuppressWarnings("unchecked")
        List<Object[]> rawRevisions = auditReader.createQuery()
                .forRevisionsOfEntity(Sample.class, false, true)
                .add(org.hibernate.envers.query.AuditEntity.id().eq(id))
                .getResultList();

        return rawRevisions.stream().map(result -> {
            Sample entity = (Sample) result[0];

            // Mapping the class
            com.kruzvinicius.limsbackend.model.Revision rev = (com.kruzvinicius.limsbackend.model.Revision) result[1];
            org.hibernate.envers.RevisionType revisionType = (org.hibernate.envers.RevisionType) result[2];

            return new AuditLogDTO(
                    rev.getId(),
                    rev.getModifiedBy(),
                    rev.getTimestamp().toString(),
                    revisionType.name(),
                    entity.getStatus()
            );
        }).toList();
    }

    @PostMapping("/{id}/results")
    public ResponseEntity<TestResult> addTestResult(@PathVariable Long id, @RequestBody TestResult result) {
        return sampleRepository.findById(id).map(sample -> {
            result.setSample(sample);
            TestResult savedResult = testResultRepository.save(result);
            return ResponseEntity.ok(savedResult);
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/results")
    public ResponseEntity<List<TestResultDTO>> getTestResultsBySampleId(@PathVariable Long id) {
        if (!sampleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        List<TestResultDTO> dtos = testResultRepository.findBySampleId(id).stream()
                .map(result -> new TestResultDTO(
                        result.getId(),
                        result.getParameterName(),
                        result.getResultValue(),
                        result.getUnit(),
                        result.getPerformedAt() != null ? result.getPerformedAt().toString() : "Not recorded"
                )).toList();
        return ResponseEntity.ok(dtos);

    }

    @GetMapping("/search")
    public ResponseEntity<SampleDTO> getSampleByBarcode(@RequestParam String barcode) {
        return sampleRepository.findByBarcode(barcode)
                .map(this::mapSampleToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    private SampleDTO mapSampleToDTO(Sample sample) {
        return new SampleDTO(
                sample.getId(),
                sample.getBarcode(),
                sample.getMaterialType(),
                sample.getStatus(),
                sample.getCustomer() != null ? sample.getCustomer().getCorporateReason() : "No customer associated",
                sample.getReceivedAt() != null ? sample.getReceivedAt().toString() : "Not recorded"
        );
    }
}

