package com.kruzvinicius.limsbackend.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@Entity
@Audited
@Slf4j
@Table(name = "test_results")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class TestResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parameter_name", nullable = false)
    private String parameterName; //e.g., "pH", "Glucose"

    @Column(name = "result_value", nullable = false)
    private String resultValue; // Using String to Allow value like "< 0.5" or "Positive"

    private String unit;

    @Column(name = "performed_at")
    private LocalDateTime performedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample; //Every result must belong to a specific sample
}

