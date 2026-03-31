package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.*;
import com.kruzvinicius.limsbackend.dto.exception.EntityNotFoundException;
import com.kruzvinicius.limsbackend.model.*;
import com.kruzvinicius.limsbackend.model.enums.SampleStatus;
import com.kruzvinicius.limsbackend.model.enums.TestResultStatus;
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

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SampleService {

    // ── Valid transitions: from → allowed next states ──────────────────────
    private static final java.util.Map<SampleStatus, Set<SampleStatus>> ALLOWED_TRANSITIONS =
            java.util.Map.of(
                    SampleStatus.PENDING_RECEIPT,       Set.of(SampleStatus.RECEIVED),
                    SampleStatus.RECEIVED,              Set.of(SampleStatus.IN_ANALYSIS, SampleStatus.REJECTED),
                    SampleStatus.IN_ANALYSIS,           Set.of(SampleStatus.PENDING_APPROVAL, SampleStatus.REJECTED),
                    SampleStatus.PENDING_APPROVAL,      Set.of(SampleStatus.APPROVED, SampleStatus.REJECTED),
                    SampleStatus.APPROVED,              Set.of(SampleStatus.RELEASED),
                    SampleStatus.RELEASED,              Set.of(),
                    SampleStatus.REJECTED,              Set.of(SampleStatus.REANALYSIS_REQUESTED),
                    SampleStatus.REANALYSIS_REQUESTED,  Set.of(SampleStatus.IN_REANALYSIS),
                    SampleStatus.IN_REANALYSIS,         Set.of(SampleStatus.PENDING_APPROVAL, SampleStatus.REJECTED)
            );

    private final SampleRepository sampleRepository;
    private final CustomerRepository customerRepository;
    private final TestResultRepository testResultRepository;
    private final UserRepository userRepository;
    private final ReanalysisRequestRepository reanalysisRequestRepository;
    private final EntityManager entityManager;

    // ── READ ─────────────────────────────────────────────────────────────────

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

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public SampleResponse create(SampleRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        Sample sample = new Sample();
        sample.setBarcode(request.barcode());
        sample.setDescription(request.description());
        sample.setMaterialType(request.materialType());
        sample.setCollectionLocation(request.collectionLocation());
        sample.setCollectionDate(request.collectionDate());
        sample.setNotes(request.notes());
        sample.setCustomer(customer);
        sample.setStatus(SampleStatus.RECEIVED);

        return mapToResponse(sampleRepository.save(sample));
    }

    // ── STATUS TRANSITION ────────────────────────────────────────────────────

    /**
     * Generic status update with state-machine validation.
     * Use the specific approve/reject/requestReanalysis methods whenever possible.
     */
    @Transactional
    public SampleResponse updateStatus(Long id, String newStatusStr) {
        Sample sample = loadSample(id);
        SampleStatus newStatus = parseStatus(newStatusStr);
        validateTransition(sample.getStatus(), newStatus);
        sample.setStatus(newStatus);
        return mapToResponse(sampleRepository.save(sample));
    }

    // ── WORKFLOW ACTIONS ──────────────────────────────────────────────────────

    /**
     * Approve a sample: moves from PENDING_APPROVAL → APPROVED.
     */
    @Transactional
    public SampleResponse approve(Long id, ApprovalRequest request) {
        Sample sample = loadSample(id);
        validateTransition(sample.getStatus(), SampleStatus.APPROVED);
        sample.setStatus(SampleStatus.APPROVED);
        log.info("Sample {} approved by {}", id, request.reviewerUsername());
        return mapToResponse(sampleRepository.save(sample));
    }

    /**
     * Release a sample: moves from APPROVED → RELEASED.
     */
    @Transactional
    public SampleResponse release(Long id) {
        Sample sample = loadSample(id);
        validateTransition(sample.getStatus(), SampleStatus.RELEASED);
        sample.setStatus(SampleStatus.RELEASED);
        return mapToResponse(sampleRepository.save(sample));
    }

    /**
     * Reject a sample: moves from any valid state → REJECTED.
     * Requires a rejection reason.
     */
    @Transactional
    public SampleResponse reject(Long id, ApprovalRequest request) {
        if (request.reason() == null || request.reason().isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }
        Sample sample = loadSample(id);
        validateTransition(sample.getStatus(), SampleStatus.REJECTED);
        sample.setStatus(SampleStatus.REJECTED);
        sample.setRejectionReason(request.reason());
        log.info("Sample {} rejected by {} — reason: {}", id, request.reviewerUsername(), request.reason());
        return mapToResponse(sampleRepository.save(sample));
    }

    /**
     * Request reanalysis after rejection: REJECTED → REANALYSIS_REQUESTED.
     */
    @Transactional
    public ReanalysisRequestDTO requestReanalysis(Long id, ReanalysisRequestDTO dto) {
        Sample sample = loadSample(id);
        validateTransition(sample.getStatus(), SampleStatus.REANALYSIS_REQUESTED);

        User requester = userRepository.findByUsername(dto.requestedBy())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + dto.requestedBy()));

        sample.setStatus(SampleStatus.REANALYSIS_REQUESTED);
        sampleRepository.save(sample);

        ReanalysisRequest req = ReanalysisRequest.builder()
                .sample(sample)
                .reason(dto.reason())
                .requestedBy(requester)
                .requestedAt(OffsetDateTime.now(ZoneOffset.UTC))
                .build();

        ReanalysisRequest saved = reanalysisRequestRepository.save(req);
        return mapToReanalysisDTO(saved);
    }

    /**
     * Returns all reanalysis requests for a sample.
     */
    @Transactional(readOnly = true)
    public List<ReanalysisRequestDTO> getReanalysisHistory(Long sampleId) {
        if (!sampleRepository.existsById(sampleId)) throw new EntityNotFoundException("Sample not found");
        return reanalysisRequestRepository.findBySampleIdOrderByRequestedAtDesc(sampleId)
                .stream().map(this::mapToReanalysisDTO).toList();
    }

    // ── TEST RESULTS ──────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<TestResultDTO> getResults(Long id) {
        if (!sampleRepository.existsById(id)) throw new EntityNotFoundException("Sample not found");
        return testResultRepository.findBySampleId(id).stream()
                .map(this::mapToTestResultDTO)
                .toList();
    }

    @Transactional
    public TestResultDTO addResult(Long id, TestResultDTO dto) {
        Sample sample = loadSample(id);

        TestResult result = new TestResult();
        result.setParameterName(dto.parameterName());
        result.setResultValue(dto.resultValue());
        result.setUnit(dto.unit());
        result.setSample(sample);
        result.setStatus(TestResultStatus.PENDING);

        return mapToTestResultDTO(testResultRepository.save(result));
    }

    // ── AUDIT HISTORY ──────────────────────────────────────────────────────────

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
                    entity.getStatus() != null ? entity.getStatus().name() : "UNKNOWN"
            );
        }).toList();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private Sample loadSample(Long id) {
        return sampleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sample not found with ID: " + id));
    }

    private SampleStatus parseStatus(String status) {
        try {
            return SampleStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }
    }

    private void validateTransition(SampleStatus current, SampleStatus next) {
        Set<SampleStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(current, Set.of());
        if (!allowed.contains(next)) {
            throw new IllegalStateException(
                    String.format("Invalid status transition: %s → %s. Allowed: %s", current, next, allowed));
        }
    }

    // ── MAPPERS ───────────────────────────────────────────────────────────────

    private SampleResponse mapToResponse(Sample sample) {
        return new SampleResponse(
                sample.getId(),
                sample.getDescription(),
                sample.getBarcode(),
                sample.getMaterialType(),
                sample.getCollectionLocation(),
                sample.getCollectionDate(),
                sample.getNotes(),
                sample.getStatus() != null ? sample.getStatus().name() : null,
                sample.getRejectionReason(),
                sample.getCustomer().getId(),
                sample.getReceivedAt()
        );
    }

    private TestResultDTO mapToTestResultDTO(TestResult r) {
        return new TestResultDTO(
                r.getId(),
                r.getParameterName(),
                r.getResultValue(),
                r.getUnit(),
                r.getPerformedAt() != null ? r.getPerformedAt().toString() : null,
                r.getStatus() != null ? r.getStatus().name() : TestResultStatus.PENDING.name(),
                r.getApprovedBy() != null ? r.getApprovedBy().getUsername() : null,
                r.getApprovedAt() != null ? r.getApprovedAt().toString() : null,
                r.getRejectionReason()
        );
    }

    private ReanalysisRequestDTO mapToReanalysisDTO(ReanalysisRequest r) {
        return new ReanalysisRequestDTO(
                r.getId(),
                r.getReason(),
                r.getRequestedAt(),
                r.getRequestedBy() != null ? r.getRequestedBy().getUsername() : null,
                r.getResolvedAt(),
                r.getResolutionNotes()
        );
    }
}