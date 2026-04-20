package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.ApprovalRequest;
import com.kruzvinicius.limsbackend.dto.TestResultDTO;
import com.kruzvinicius.limsbackend.dto.exception.EntityNotFoundException;
import com.kruzvinicius.limsbackend.model.TestResult;
import com.kruzvinicius.limsbackend.model.User;
import com.kruzvinicius.limsbackend.model.enums.TestResultStatus;
import com.kruzvinicius.limsbackend.repository.TestResultRepository;
import com.kruzvinicius.limsbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Service for the test result approval workflow.
 * Supervisors approve or reject individual analytical results before sample release.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TestResultService {

    private final TestResultRepository testResultRepository;
    private final UserRepository userRepository;

    @Transactional
    public TestResultDTO updateResultValue(Long resultId, String resultValue) {
        TestResult result = loadResult(resultId);
        validatePending(result);

        result.setResultValue(resultValue);
        result.setPerformedAt(OffsetDateTime.now(ZoneOffset.UTC));

        log.info("Test result {} value updated to {}", resultId, resultValue);
        return mapToDTO(testResultRepository.save(result));
    }

    /**
     * Approve an individual test result.
     * Moves result from PENDING → APPROVED.
     */
    @Transactional
    public TestResultDTO approveResult(Long resultId, ApprovalRequest request) {
        TestResult result = loadResult(resultId);
        validatePending(result);

        User reviewer = loadUser(request.reviewerUsername());

        result.setStatus(TestResultStatus.APPROVED);
        result.setApprovedBy(reviewer);
        result.setApprovedAt(OffsetDateTime.now(ZoneOffset.UTC));

        log.info("Test result {} approved by {}", resultId, reviewer.getUsername());
        return mapToDTO(testResultRepository.save(result));
    }

    /**
     * Reject an individual test result.
     * Moves result from PENDING → REJECTED; requires a reason.
     */
    @Transactional
    public TestResultDTO rejectResult(Long resultId, ApprovalRequest request) {
        if (request.reason() == null || request.reason().isBlank()) {
            throw new IllegalArgumentException("Rejection reason is required");
        }

        TestResult result = loadResult(resultId);
        validatePending(result);

        User reviewer = loadUser(request.reviewerUsername());

        result.setStatus(TestResultStatus.REJECTED);
        result.setApprovedBy(reviewer);
        result.setApprovedAt(OffsetDateTime.now(ZoneOffset.UTC));
        result.setRejectionReason(request.reason());

        log.info("Test result {} rejected by {} — reason: {}", resultId, reviewer.getUsername(), request.reason());
        return mapToDTO(testResultRepository.save(result));
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private TestResult loadResult(Long id) {
        return testResultRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Test result not found: " + id));
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    private void validatePending(TestResult result) {
        if (result.getStatus() != TestResultStatus.PENDING) {
            throw new IllegalStateException(
                    "Result is already " + result.getStatus() + ". Only PENDING results can be reviewed.");
        }
    }

    // ── MAPPER ────────────────────────────────────────────────────────────────

    private TestResultDTO mapToDTO(TestResult r) {
        return new TestResultDTO(
                r.getId(),
                r.getParameterName(),
                r.getResultValue(),
                r.getUnit(),
                r.getPerformedAt() != null ? r.getPerformedAt().toString() : null,
                r.getStatus() != null ? r.getStatus().name() : null,
                r.getApprovedBy() != null ? r.getApprovedBy().getUsername() : null,
                r.getApprovedAt() != null ? r.getApprovedAt().toString() : null,
                r.getRejectionReason()
        );
    }
}
