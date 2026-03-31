package com.kruzvinicius.limsbackend.model;

import com.kruzvinicius.limsbackend.model.enums.EquipmentStatus;
import com.kruzvinicius.limsbackend.model.enums.EquipmentType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a piece of laboratory equipment tracked by the LIMS.
 * Each equipment has its own calibration and maintenance history.
 */
@Entity
@Audited
@Table(name = "equipment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String model;

    @Column(name = "serial_number", unique = true)
    private String serialNumber;

    private String manufacturer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentType type;

    /** Physical location in the lab (e.g., "Room 3 — Bench B"). */
    private String location;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status = EquipmentStatus.ACTIVE;

    @Column(name = "purchased_at")
    private LocalDate purchasedAt;

    /** Date by which the next calibration must be completed. */
    @Column(name = "next_calibration_due")
    private LocalDate nextCalibrationDue;

    /** Date by which the next preventive maintenance must be performed. */
    @Column(name = "next_maintenance_due")
    private LocalDate nextMaintenanceDue;

    @Builder.Default
    @Column(name = "registered_at", nullable = false)
    private OffsetDateTime registeredAt = OffsetDateTime.now(ZoneOffset.UTC);

    @Builder.Default
    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalibrationRecord> calibrationRecords = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "equipment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MaintenanceRecord> maintenanceRecords = new ArrayList<>();
}
