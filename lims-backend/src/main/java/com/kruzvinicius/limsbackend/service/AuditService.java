package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.AuditLogDTO;
import com.kruzvinicius.limsbackend.model.Revision;
import com.kruzvinicius.limsbackend.model.Sample;
import com.kruzvinicius.limsbackend.model.TestResult;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Centralised service for querying the Hibernate Envers audit trail.
 * Covers revision history for samples, test results, and cross-entity queries.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final EntityManager entityManager;

    // ── SAMPLE HISTORY ────────────────────────────────────────────────────────

    /**
     * Returns the full change history for a Sample, including who made each change
     * and the status at that point in time.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<AuditLogDTO> getSampleHistory(Long sampleId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);

        List<Object[]> revisions = reader.createQuery()
                .forRevisionsOfEntity(Sample.class, false, true)
                .add(AuditEntity.id().eq(sampleId))
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();

        return buildDiff(revisions, "Sample", sampleId,
                rev -> ((Sample) rev).getStatus() != null ? ((Sample) rev).getStatus().name() : null);
    }

    // ── TEST RESULT HISTORY ────────────────────────────────────────────────────

    /**
     * Returns the versioned history for a TestResult, including diffs on the result value.
     */
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<AuditLogDTO> getResultHistory(Long resultId) {
        AuditReader reader = AuditReaderFactory.get(entityManager);

        List<Object[]> revisions = reader.createQuery()
                .forRevisionsOfEntity(TestResult.class, false, true)
                .add(AuditEntity.id().eq(resultId))
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();

        return buildDiff(revisions, "TestResult", resultId,
                rev -> {
                    TestResult r = (TestResult) rev;
                    return r.getParameterName() + " = " + r.getResultValue() + " " + r.getUnit()
                            + " [" + (r.getStatus() != null ? r.getStatus().name() : "PENDING") + "]";
                });
    }

    // ── SHARED DIFF BUILDER ────────────────────────────────────────────────────

    @FunctionalInterface
    private interface ValueExtractor {
        String extract(Object entity);
    }

    private List<AuditLogDTO> buildDiff(List<Object[]> revisions, String entityType, Long entityId,
                                         ValueExtractor extractor) {
        String[] previousRef = {null};

        return revisions.stream().map(entry -> {
            Object entity = entry[0];
            Revision rev = (Revision) entry[1];
            RevisionType revType = (RevisionType) entry[2];

            String action = switch (revType) {
                case ADD -> "CREATED";
                case MOD -> "MODIFIED";
                case DEL -> "DELETED";
                default -> "UNKNOWN";
            };

            String currentValue = entity != null ? extractor.extract(entity) : null;

            AuditLogDTO dto = new AuditLogDTO(
                    rev.getId(),
                    rev.getModifiedBy(),
                    rev.getTimestamp().toString(),
                    action,
                    currentValue,
                    entityType,
                    entityId,
                    previousRef[0],
                    currentValue
            );

            previousRef[0] = currentValue;
            return dto;
        }).toList();
    }
}
