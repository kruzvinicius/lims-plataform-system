package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.NonConformanceDTO;
import com.kruzvinicius.limsbackend.dto.exception.EntityNotFoundException;
import com.kruzvinicius.limsbackend.model.*;
import com.kruzvinicius.limsbackend.model.enums.NonConformanceSeverity;
import com.kruzvinicius.limsbackend.model.enums.NonConformanceStatus;
import com.kruzvinicius.limsbackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Service for managing the non-conformance lifecycle:
 * OPEN → UNDER_INVESTIGATION → RESOLVED → CLOSED
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NonConformanceService {

    private final NonConformanceRepository ncRepository;
    private final UserRepository userRepository;
    private final SampleRepository sampleRepository;
    private final TestResultRepository testResultRepository;
    private final NotificationService notificationService;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public NonConformanceDTO create(NonConformanceDTO dto) {
        User detectedBy = loadUser(dto.detectedBy());

        Sample sample = dto.sampleId() != null
                ? sampleRepository.findById(dto.sampleId())
                        .orElseThrow(() -> new EntityNotFoundException("Sample not found: " + dto.sampleId()))
                : null;

        TestResult testResult = dto.testResultId() != null
                ? testResultRepository.findById(dto.testResultId())
                        .orElseThrow(() -> new EntityNotFoundException("TestResult not found: " + dto.testResultId()))
                : null;

        NonConformance nc = NonConformance.builder()
                .title(dto.title())
                .description(dto.description())
                .type(dto.type())
                .severity(dto.severity())
                .status(NonConformanceStatus.OPEN)
                .detectedBy(detectedBy)
                .sample(sample)
                .testResult(testResult)
                .build();

        log.info("Non-conformance created: [{}] by {}", dto.title(), dto.detectedBy());
        NonConformanceDTO saved = mapToDTO(ncRepository.save(nc));

        // Notify managers about new non-conformance
        notificationService.sendNonConformanceAlert(
                "manager@lims.local",
                dto.title(),
                dto.severity() != null ? dto.severity().name() : "UNKNOWN"
        );

        return saved;
    }

    // ── WORKFLOW ACTIONS ──────────────────────────────────────────────────────

    /** Assign a responsible investigator — moves to UNDER_INVESTIGATION. */
    @Transactional
    public NonConformanceDTO assign(Long id, String assigneeUsername) {
        NonConformance nc = loadNC(id);
        User assignee = loadUser(assigneeUsername);
        nc.setAssignedTo(assignee);
        nc.setStatus(NonConformanceStatus.UNDER_INVESTIGATION);
        return mapToDTO(ncRepository.save(nc));
    }

    /** Record root cause — requires UNDER_INVESTIGATION status. */
    @Transactional
    public NonConformanceDTO investigate(Long id, String rootCause) {
        NonConformance nc = loadNC(id);
        if (nc.getStatus() != NonConformanceStatus.UNDER_INVESTIGATION) {
            throw new IllegalStateException("NC must be UNDER_INVESTIGATION to record root cause");
        }
        nc.setRootCause(rootCause);
        return mapToDTO(ncRepository.save(nc));
    }

    /** Resolve the NC with corrective + preventive actions — moves to RESOLVED. */
    @Transactional
    public NonConformanceDTO resolve(Long id, String correctiveAction, String preventiveAction) {
        NonConformance nc = loadNC(id);
        if (nc.getStatus() != NonConformanceStatus.UNDER_INVESTIGATION) {
            throw new IllegalStateException("NC must be UNDER_INVESTIGATION before resolving");
        }
        nc.setCorrectiveAction(correctiveAction);
        nc.setPreventiveAction(preventiveAction);
        nc.setStatus(NonConformanceStatus.RESOLVED);
        nc.setResolvedAt(OffsetDateTime.now(ZoneOffset.UTC));
        return mapToDTO(ncRepository.save(nc));
    }

    /** Close the NC — final state after reviewing preventive actions. */
    @Transactional
    public NonConformanceDTO close(Long id) {
        NonConformance nc = loadNC(id);
        if (nc.getStatus() != NonConformanceStatus.RESOLVED) {
            throw new IllegalStateException("NC must be RESOLVED before closing");
        }
        nc.setStatus(NonConformanceStatus.CLOSED);
        return mapToDTO(ncRepository.save(nc));
    }

    // ── READ ──────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public NonConformanceDTO findById(Long id) {
        return mapToDTO(loadNC(id));
    }

    @Transactional(readOnly = true)
    public List<NonConformanceDTO> findAll(NonConformanceStatus status, NonConformanceSeverity severity) {
        List<NonConformance> results;
        if (status != null && severity != null) {
            results = ncRepository.findByStatusAndSeverity(status, severity);
        } else if (status != null) {
            results = ncRepository.findByStatus(status);
        } else if (severity != null) {
            results = ncRepository.findBySeverity(severity);
        } else {
            results = ncRepository.findAll();
        }
        return results.stream().map(this::mapToDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<NonConformanceDTO> findBySample(Long sampleId) {
        return ncRepository.findBySampleId(sampleId).stream().map(this::mapToDTO).toList();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private NonConformance loadNC(Long id) {
        return ncRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Non-conformance not found: " + id));
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    // ── MAPPER ────────────────────────────────────────────────────────────────

    private NonConformanceDTO mapToDTO(NonConformance nc) {
        return new NonConformanceDTO(
                nc.getId(),
                nc.getTitle(),
                nc.getDescription(),
                nc.getType(),
                nc.getSeverity(),
                nc.getStatus(),
                nc.getDetectedAt(),
                nc.getResolvedAt(),
                nc.getRootCause(),
                nc.getCorrectiveAction(),
                nc.getPreventiveAction(),
                nc.getDetectedBy() != null ? nc.getDetectedBy().getUsername() : null,
                nc.getAssignedTo() != null ? nc.getAssignedTo().getUsername() : null,
                nc.getSample() != null ? nc.getSample().getId() : null,
                nc.getTestResult() != null ? nc.getTestResult().getId() : null
        );
    }
}
