package com.kruzvinicius.limsbackend.model;

import com.kruzvinicius.limsbackend.model.enums.NonConformanceSeverity;
import com.kruzvinicius.limsbackend.model.enums.NonConformanceStatus;
import com.kruzvinicius.limsbackend.model.enums.NonConformanceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Records a non-conformance event detected during laboratory operations.
 * Covers result deviations, process failures, QC issues and equipment problems.
 * Full lifecycle: OPEN → UNDER_INVESTIGATION → RESOLVED → CLOSED.
 */
@Entity
@Audited
@Table(name = "non_conformances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NonConformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NonConformanceType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NonConformanceSeverity severity;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NonConformanceStatus status = NonConformanceStatus.OPEN;

    @Builder.Default
    @Column(name = "detected_at", nullable = false)
    private OffsetDateTime detectedAt = OffsetDateTime.now(ZoneOffset.UTC);

    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    /** Root cause analysis — filled during UNDER_INVESTIGATION. */
    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    /** Immediate corrective action taken — filled when resolving. */
    @Column(name = "corrective_action", columnDefinition = "TEXT")
    private String correctiveAction;

    /** Long-term preventive measure to avoid recurrence — filled when closing. */
    @Column(name = "preventive_action", columnDefinition = "TEXT")
    private String preventiveAction;

    /** User who detected and registered the NC. */
    @ManyToOne
    @JoinColumn(name = "detected_by", nullable = false)
    private User detectedBy;

    /** User responsible for investigating and resolving the NC. */
    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    /** Sample related to this NC (optional). */
    @ManyToOne
    @JoinColumn(name = "sample_id")
    private Sample sample;

    /** Specific test result that triggered this NC (optional). */
    @ManyToOne
    @JoinColumn(name = "test_result_id")
    private TestResult testResult;
}
