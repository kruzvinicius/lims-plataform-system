package com.kruzvinicius.limsbackend.model;

import com.kruzvinicius.limsbackend.model.enums.TestResultStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Entity representing an analytical result for a specific sample.
 * Follows an approval flow: PENDING → APPROVED / REJECTED.
 */
@Entity
@Audited
@Table(name = "tests_results")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parameter_name", nullable = false)
    private String parameterName;

    @Column(name = "result_value", nullable = false)
    private String resultValue;

    @Column(nullable = false)
    private String unit;

    @Column(name = "performed_at")
    private OffsetDateTime performedAt = OffsetDateTime.now(ZoneOffset.UTC);

    /** Approval status of this result. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TestResultStatus status = TestResultStatus.PENDING;

    /** Supervisor who approved or rejected this result. */
    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    /** Timestamp of the approval/rejection decision. */
    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;

    /** Reason provided when rejecting the result. */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;
}