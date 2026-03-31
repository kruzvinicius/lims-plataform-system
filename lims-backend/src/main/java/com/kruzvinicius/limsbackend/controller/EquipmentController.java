package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.dto.CalibrationRecordDTO;
import com.kruzvinicius.limsbackend.dto.EquipmentDTO;
import com.kruzvinicius.limsbackend.dto.MaintenanceRecordDTO;
import com.kruzvinicius.limsbackend.model.enums.EquipmentStatus;
import com.kruzvinicius.limsbackend.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Equipment management.
 * CRUD, calibration, maintenance and due-date alerts.
 */
@RestController
@RequestMapping("/api/equipment")
@Slf4j
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<EquipmentDTO> create(@Valid @RequestBody EquipmentDTO dto) {
        log.info("REST request to register equipment: {}", dto.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.create(dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<List<EquipmentDTO>> getAll() {
        return ResponseEntity.ok(equipmentService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<EquipmentDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.findById(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<EquipmentDTO> updateStatus(
            @PathVariable Long id, @RequestBody EquipmentStatus status) {
        return ResponseEntity.ok(equipmentService.updateStatus(id, status));
    }

    @GetMapping("/due-calibration")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<EquipmentDTO>> dueCalibration(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(equipmentService.findDueForCalibration(days));
    }

    @GetMapping("/due-maintenance")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<EquipmentDTO>> dueMaintenance(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(equipmentService.findDueForMaintenance(days));
    }

    @PostMapping("/{id}/calibrations")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST')")
    public ResponseEntity<CalibrationRecordDTO> addCalibration(
            @PathVariable Long id, @Valid @RequestBody CalibrationRecordDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.addCalibration(id, dto));
    }

    @GetMapping("/{id}/calibrations")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<List<CalibrationRecordDTO>> getCalibrations(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getCalibrations(id));
    }

    @PostMapping("/{id}/maintenance")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<MaintenanceRecordDTO> addMaintenance(
            @PathVariable Long id, @Valid @RequestBody MaintenanceRecordDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipmentService.addMaintenance(id, dto));
    }

    @GetMapping("/{id}/maintenance")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<List<MaintenanceRecordDTO>> getMaintenance(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getMaintenanceRecords(id));
    }
}
