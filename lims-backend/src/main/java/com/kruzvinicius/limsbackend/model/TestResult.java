package com.kruzvinicius.limsbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Entity representing an analytical result for a specific sample.
 * Audited to track any changes in the values provided by the lab.
 */
@Entity
@Audited
@Table(name = "tests_results") // Matches SQL 'tests_results'
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parameter_name", nullable = false)
    private String parameterName; // e.g., "pH", "Glucose", "Lead Content"

    @Column(name = "result_value", nullable = false)
    private String resultValue; // String allows values like "< 0.5", "Positive", or "10.5"

    @Column(nullable = false)
    private String unit; // e.g., "mg/L", "mg/kg", "ppm"

    @Column(name = "performed_at")
    private OffsetDateTime performedAt = OffsetDateTime.now(ZoneOffset.UTC);

    @ManyToOne
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample; // Every result must belong to a specific sample
}