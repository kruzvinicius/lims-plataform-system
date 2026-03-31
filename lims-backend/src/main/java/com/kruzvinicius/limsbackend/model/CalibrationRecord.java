package com.kruzvinicius.limsbackend.model;

import com.kruzvinicius.limsbackend.model.enums.CalibrationResult;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Records a single calibration event for a piece of equipment.
 * Calibration history is mandatory for ISO/IEC 17025 compliance.
 */
@Entity
@Audited
@Table(name = "calibration_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalibrationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Builder.Default
    @Column(name = "performed_at", nullable = false)
    private OffsetDateTime performedAt = OffsetDateTime.now(ZoneOffset.UTC);

    /** Date by which the next calibration must be completed. */
    @Column(name = "next_due_at")
    private LocalDate nextDueAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalibrationResult result;

    /** Internal staff member who performed the calibration (optional if external). */
    @ManyToOne
    @JoinColumn(name = "performed_by")
    private User performedBy;

    /** Name of the external calibration provider (e.g., accredited lab). */
    @Column(name = "external_provider")
    private String externalProvider;

    /** Reference or identifier of the calibration certificate issued. */
    @Column(name = "certificate_reference")
    private String certificateReference;

    /** Free-text observations or measurement deviations found. */
    @Column(columnDefinition = "TEXT")
    private String observations;

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;
}
