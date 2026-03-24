package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.*;
import com.kruzvinicius.limsbackend.dto.exception.EntityNotFoundException;
import com.kruzvinicius.limsbackend.model.*;
import com.kruzvinicius.limsbackend.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SampleService {

    private final SampleRepository sampleRepository;
    private final CustomerRepository customerRepository;
    private final TestResultRepository testResultRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<SampleResponse> findAll(Pageable pageable) {
        return sampleRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public SampleResponse findById(Long id) {
        return sampleRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new EntityNotFoundException("Sample not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<SampleResponse> findByCustomer(Long customerId) {
        return sampleRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<SampleResponse> findByBarcode(String barcode) {
        return sampleRepository.findByBarcode(barcode).map(this::mapToResponse);
    }

    @Transactional
    public SampleResponse create(SampleRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        Sample sample = new Sample();
        sample.setBarcode(request.barcode());
        sample.setDescription(request.description());
        sample.setMaterialType(request.materialType());
        sample.setCustomer(customer);
        sample.setStatus("RECEIVED");

        return mapToResponse(sampleRepository.save(sample));
    }

    @Transactional
    public SampleResponse updateStatus(Long id, String status) {
        Sample sample = sampleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sample not found"));
        sample.setStatus(status);
        return mapToResponse(sampleRepository.save(sample));
    }

    @Transactional(readOnly = true)
    public List<TestResultDTO> getResults(Long id) {
        if (!sampleRepository.existsById(id)) throw new EntityNotFoundException("Sample not found");
        return testResultRepository.findBySampleId(id).stream()
                .map(res -> new TestResultDTO(
                        res.getId(),
                        res.getParameterName(),
                        res.getResultValue(),
                        res.getUnit(),
                        res.getPerformedAt() != null ? res.getPerformedAt().toString() : "Pending"
                ))
                .toList();
    }

    @Transactional
    public TestResultDTO addResult(Long id, TestResultDTO dto) {
        Sample sample = sampleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sample not found"));

        TestResult result = new TestResult();
        result.setParameterName(dto.parameterName());
        result.setResultValue(dto.resultValue());
        result.setUnit(dto.unit());
        result.setSample(sample);

        TestResult saved = testResultRepository.save(result);
        return new TestResultDTO(saved.getId(), saved.getParameterName(),
                saved.getResultValue(), saved.getUnit(), "Recorded");
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<AuditLogDTO> getHistory(Long id) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        List<Object[]> rawRevisions = auditReader.createQuery()
                .forRevisionsOfEntity(Sample.class, false, true)
                .add(AuditEntity.id().eq(id))
                .getResultList();

        return rawRevisions.stream().map(result -> {
            Sample entity = (Sample) result[0];
            Revision rev = (Revision) result[1];
            return new AuditLogDTO(
                    rev.getId(),
                    rev.getModifiedBy(),
                    rev.getTimestamp().toString(),
                    "MODIFIED",
                    entity.getStatus()
            );
        }).toList();
    }

    /**
     * Mapper: Converts Entity to Response DTO with Date conversion
     */
    private SampleResponse mapToResponse(Sample sample) {
        return new SampleResponse(
                sample.getId(),
                sample.getDescription(),
                sample.getBarcode(),
                sample.getMaterialType(),
                sample.getStatus(),
                sample.getCustomer().getId(),
                sample.getReceivedAt()
        );
    }
}