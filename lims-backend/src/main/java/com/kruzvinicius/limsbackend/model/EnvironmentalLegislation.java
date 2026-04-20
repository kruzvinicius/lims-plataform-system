package com.kruzvinicius.limsbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an environmental legislation (e.g., CONAMA 357, Portaria SVSA 888/2021).
 * Each legislation defines a set of VMPs (Valores Máximos Permitidos) for given parameters.
 */
@Entity
@Table(name = "environmental_legislations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnvironmentalLegislation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Short identifier (e.g., "CONAMA-357-II", "PORT-888-2021"). */
    @Column(unique = true, nullable = false, length = 50)
    private String code;

    /** Full descriptive name. */
    @Column(nullable = false)
    private String name;

    /** Geographic scope or region of applicability (e.g., "Brasil", "São Paulo"). */
    @Column(length = 100)
    private String region;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);

    /** The set of VMPs defined by this legislation for each parameter. */
    @Builder.Default
    @OneToMany(mappedBy = "legislation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LegislationParameter> parameters = new ArrayList<>();
}
