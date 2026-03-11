package com.kruzvinicius.limsbackend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.persistence.*;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "samples")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Sample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String barcode; // unique sample barcode

    @Column(name = "material_type")
    private String materialType; // Ex.: Water, Blood, Soil

    private String status = "Received"; // Ex.: Received, Shipped, Delivered

    @Column(name = "received_at")
    private LocalDateTime receivedAt = LocalDateTime.now(); // Date and time when the sample was received

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

}
