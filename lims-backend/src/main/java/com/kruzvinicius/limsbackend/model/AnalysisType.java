package com.kruzvinicius.limsbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

/**
 * Defines a type of laboratory analysis/test with acceptance ranges.
 * Used for automatic validation of results against min/max limits.
 */
@Entity
@Audited
@Table(name = "analysis_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Short code (e.g., "PH", "TURB", "COL-TOTAL"). */
    @Column(unique = true, nullable = false, length = 50)
    private String code;

    /** Full name (e.g., "pH", "Turbidez", "Coliformes Totais"). */
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    /** Default unit of measurement (e.g., "mg/L", "NTU", "UFC/mL"). */
    @Column(name = "default_unit", length = 30)
    private String defaultUnit;

    /** Minimum acceptable value. Result below this triggers OUT_OF_RANGE warning. */
    @Column(name = "min_value", precision = 12, scale = 4)
    private BigDecimal minValue;

    /** Maximum acceptable value. Result above this triggers OUT_OF_RANGE warning. */
    @Column(name = "max_value", precision = 12, scale = 4)
    private BigDecimal maxValue;

    /** Whether this analysis type is currently active and available for use. */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
