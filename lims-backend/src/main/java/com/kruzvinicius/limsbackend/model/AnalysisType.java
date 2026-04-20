package com.kruzvinicius.limsbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

/**
 * Defines a type of laboratory analysis/test.
 * Acceptance limits (VMP) belong to the applicable legislation, not to the parameter.
 * The parameter carries its measurement uncertainty (u), which is method-specific.
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

    /**
     * Expanded measurement uncertainty of the analytical method (e.g., 0.05 means ±0.05 in default unit).
     * This is an intrinsic property of the method, not a legal limit.
     */
    @Column(name = "uncertainty_value", precision = 12, scale = 6)
    private BigDecimal uncertaintyValue;

    /** Default commercial price for proposals. */
    @Column(name = "default_price", precision = 12, scale = 2)
    private BigDecimal defaultPrice;

    /** Whether this analysis type is currently active and available for use. */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}
