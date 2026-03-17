package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.exception.EntityNotFoundException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SampleService {

    private static final String SAMPLE_NOT_FOUND = "Sample not found";

    private final SampleRepository sampleRepository;
    private final TestResultRepository testResultRepository;
    private final EntityManager entityManager;


    public Page<SampleDTO> findAll(Pageable pageable) {
        log.info("Fetching paginated samples - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

        return sampleRepository.findAll(pageable)
                .map(SampleDTO::fromEntity);
    }

    public List<SampleDTO> findByCustomer(Long customerId) {
        return sampleRepository.findByCustomerId(customerId).stream()
                .map(SampleDTO::fromEntity)
                .toList();
    }

    @Transactional
    public SampleDTO create(SampleDTO dto) {
        Sample sample = new Sample();
        sample.setBarcode(dto.getBarcode());
        sample.setMaterialType(dto.getMaterialType());
        sample.setStatus(dto.getStatus() != null ? dto.getStatus() : "RECEIVED");

        Sample savedSample = sampleRepository.save(sample);
        log.info("New sample created with barcode: {}", savedSample.getBarcode());
        return SampleDTO.fromEntity(savedSample);
    }

    public SampleDTO findById(Long id) {
        return sampleRepository.findById(id)
                .map(SampleDTO::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException(SAMPLE_NOT_FOUND + " with id: " + id));
    }

    @Transactional
    public Sample updateStatus(Long id, String status) {
        Sample sample = sampleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(SAMPLE_NOT_FOUND));
        sample.setStatus(status);
        return sampleRepository.save(sample);
    }

    public List<AuditLogDTO> getHistory(Long id) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);

        @SuppressWarnings("unchecked")
        List<Object[]> rawRevisions = auditReader.createQuery()
                .forRevisionsOfEntity(Sample.class, false, true)
                .add(org.hibernate.envers.query.AuditEntity.id().eq(id))
                .getResultList();

        return rawRevisions.stream().map(result -> {
            Sample entity = (Sample) result[0];
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

    @Transactional
    public TestResult addResult(Long id, TestResult result) {
        Sample sample = sampleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(SAMPLE_NOT_FOUND));
        result.setSample(sample);
        return testResultRepository.save(result);
    }

    public List<TestResultDTO> getResults(Long id) {
        if (!sampleRepository.existsById(id)) {
            throw new EntityNotFoundException(SAMPLE_NOT_FOUND);
        }
        return testResultRepository.findBySampleId(id).stream()
                .map(result -> new TestResultDTO(
                        result.getId(),
                        result.getParameterName(),
                        result.getResultValue(),
                        result.getUnit(),
                        result.getPerformedAt() != null ? result.getPerformedAt().toString() : "Not recorded"
                )).toList();
    }

    public Optional<SampleDTO> findByBarcode(String barcode) {
        return sampleRepository.findByBarcode(barcode).map(SampleDTO::fromEntity);
    }
}