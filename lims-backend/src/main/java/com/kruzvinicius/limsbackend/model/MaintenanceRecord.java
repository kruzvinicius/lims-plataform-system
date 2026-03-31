package com.kruzvinicius.limsbackend.model;

import com.kruzvinicius.limsbackend.model.enums.MaintenanceType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Records a single maintenance event (preventive or corrective) for a piece of equipment.
 */
@Entity
@Audited
@Table(name = "maintenance_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MaintenanceType type;

    @Builder.Default
    @Column(name = "performed_at", nullable = false)
    private OffsetDateTime performedAt = OffsetDateTime.now(ZoneOffset.UTC);

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /** How the problem was resolved (for corrective maintenance). */
    @Column(columnDefinition = "TEXT")
    private String resolution;

    /** Cost of the maintenance (optional, for budget tracking). */
    @Column(precision = 12, scale = 2)
    private BigDecimal cost;

    /** Internal technician who performed maintenance. */
    @ManyToOne
    @JoinColumn(name = "performed_by")
    private User performedBy;

    /** Name of the external service provider. */
    @Column(name = "external_provider")
    private String externalProvider;

    @ManyToOne
    @JoinColumn(name = "equipment_id", nullable = false)
    private Equipment equipment;
}
