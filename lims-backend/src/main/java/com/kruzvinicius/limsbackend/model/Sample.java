package com.kruzvinicius.limsbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Entity
@Audited
@Table(name = "samples")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ADD THIS FIELD TO FIX THE SERVICE ERRORS
    @Column(nullable = false)
    private String description;

    @Column(unique = true, nullable = false)
    private String barcode;

    @Column(name = "material_type")
    private String materialType; // e.g., Water, Blood, Soil

    @Column(nullable = false)
    private String status = "RECEIVED";

    @Column(name = "received_at")
    private OffsetDateTime receivedAt = OffsetDateTime.now(ZoneOffset.UTC);

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
}