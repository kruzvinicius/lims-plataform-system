package com.kruzvinicius.limsbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Defines a VMP (Valor Máximo Permitido) for a specific analysis parameter
 * within the context of a given environmental legislation.
 */
@Entity
@Table(name = "legislation_parameters",
        uniqueConstraints = @UniqueConstraint(columnNames = {"legislation_id", "analysis_type_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LegislationParameter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "legislation_id", nullable = false)
    private EnvironmentalLegislation legislation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_type_id", nullable = false)
    private AnalysisType analysisType;

    /** Minimum permitted value for this parameter under this legislation (optional). */
    @Column(name = "vmp_min", precision = 14, scale = 6)
    private BigDecimal vmpMin;

    /** Maximum permitted value for this parameter under this legislation. */
    @Column(name = "vmp_max", nullable = false, precision = 14, scale = 6)
    private BigDecimal vmpMax;

    /** Unit of measurement for the VMP (may differ from the parameter's default unit). */
    @Column(length = 30)
    private String unit;

    /** Optional notes (e.g., conditions, exceptions). */
    @Column(columnDefinition = "TEXT")
    private String notes;
}
