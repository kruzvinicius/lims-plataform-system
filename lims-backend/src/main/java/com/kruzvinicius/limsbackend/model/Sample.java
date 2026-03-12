package com.kruzvinicius.limsbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

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
