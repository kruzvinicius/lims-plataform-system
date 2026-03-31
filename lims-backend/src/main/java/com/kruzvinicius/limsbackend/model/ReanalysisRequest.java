package com.kruzvinicius.limsbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Represents a formal request to reanalyse a sample after it has been rejected.
 * Captures who requested it, why, and how it was resolved.
 */
@Entity
@Audited
@Table(name = "reanalysis_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReanalysisRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Justification for requesting the reanalysis. */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    /** When the reanalysis was requested. */
    @Builder.Default
    @Column(name = "requested_at", nullable = false)
    private OffsetDateTime requestedAt = OffsetDateTime.now(ZoneOffset.UTC);

    /** When the reanalysis was resolved (approved/completed). Null if pending. */
    @Column(name = "resolved_at")
    private OffsetDateTime resolvedAt;

    /** Notes on how the reanalysis was resolved. */
    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    /** User who requested the reanalysis. */
    @ManyToOne
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    /** The sample being reanalysed. */
    @ManyToOne
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;
}
