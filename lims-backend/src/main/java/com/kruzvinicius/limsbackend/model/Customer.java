package com.kruzvinicius.limsbackend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "customers")
@Data
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "corporate_reason")
    private String corporateReason;

    private String email;
    private String phone;

    @Column(name = "tax_id")
    private String taxId;
}

