package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.CalibrationRecordDTO;
import com.kruzvinicius.limsbackend.dto.EquipmentDTO;
import com.kruzvinicius.limsbackend.dto.MaintenanceRecordDTO;
import com.kruzvinicius.limsbackend.dto.exception.EntityNotFoundException;
import com.kruzvinicius.limsbackend.model.*;
import com.kruzvinicius.limsbackend.model.enums.CalibrationResult;
import com.kruzvinicius.limsbackend.model.enums.EquipmentStatus;
import com.kruzvinicius.limsbackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for equipment management, including calibration and maintenance records.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final CalibrationRecordRepository calibrationRepository;
    private final MaintenanceRecordRepository maintenanceRepository;
    private final UserRepository userRepository;

    // ── EQUIPMENT ─────────────────────────────────────────────────────────────

    @Transactional
    public EquipmentDTO create(EquipmentDTO dto) {
        Equipment equipment = Equipment.builder()
                .name(dto.name())
                .model(dto.model())
                .serialNumber(dto.serialNumber())
                .manufacturer(dto.manufacturer())
                .type(dto.type())
                .location(dto.location())
                .status(dto.status() != null ? dto.status() : EquipmentStatus.ACTIVE)
                .purchasedAt(dto.purchasedAt())
                .nextCalibrationDue(dto.nextCalibrationDue())
                .nextMaintenanceDue(dto.nextMaintenanceDue())
                .build();
        return mapEquipmentToDTO(equipmentRepository.save(equipment));
    }

    @Transactional(readOnly = true)
    public EquipmentDTO findById(Long id) {
        return mapEquipmentToDTO(loadEquipment(id));
    }

    @Transactional(readOnly = true)
    public List<EquipmentDTO> findAll() {
        return equipmentRepository.findAll().stream().map(this::mapEquipmentToDTO).toList();
    }

    @Transactional
    public EquipmentDTO updateStatus(Long id, EquipmentStatus status) {
        Equipment equipment = loadEquipment(id);
        equipment.setStatus(status);
        return mapEquipmentToDTO(equipmentRepository.save(equipment));
    }

    /**
     * Returns equipment whose next calibration is due within the next {@code daysBefore} days
     * (including already overdue ones).
     */
    @Transactional(readOnly = true)
    public List<EquipmentDTO> findDueForCalibration(int daysBefore) {
        LocalDate threshold = LocalDate.now().plusDays(daysBefore);
        return equipmentRepository.findDueForCalibration(threshold)
                .stream().map(this::mapEquipmentToDTO).toList();
    }

    /**
     * Returns equipment whose next maintenance is due within the next {@code daysBefore} days.
     */
    @Transactional(readOnly = true)
    public List<EquipmentDTO> findDueForMaintenance(int daysBefore) {
        LocalDate threshold = LocalDate.now().plusDays(daysBefore);
        return equipmentRepository.findDueForMaintenance(threshold)
                .stream().map(this::mapEquipmentToDTO).toList();
    }

    // ── CALIBRATION ───────────────────────────────────────────────────────────

    @Transactional
    public CalibrationRecordDTO addCalibration(Long equipmentId, CalibrationRecordDTO dto) {
        Equipment equipment = loadEquipment(equipmentId);

        User performedBy = dto.performedBy() != null
                ? userRepository.findByUsername(dto.performedBy()).orElse(null)
                : null;

        CalibrationRecord record = CalibrationRecord.builder()
                .equipment(equipment)
                .result(dto.result())
                .nextDueAt(dto.nextDueAt())
                .performedBy(performedBy)
                .externalProvider(dto.externalProvider())
                .certificateReference(dto.certificateReference())
                .observations(dto.observations())
                .build();

        // If the calibration has a due date, update the equipment's next due date
        if (dto.nextDueAt() != null) {
            equipment.setNextCalibrationDue(dto.nextDueAt());
        }

        // If calibration failed, put equipment under maintenance
        if (dto.result() == CalibrationResult.FAIL) {
            equipment.setStatus(EquipmentStatus.UNDER_MAINTENANCE);
            log.warn("Equipment {} FAILED calibration — status set to UNDER_MAINTENANCE", equipmentId);
        }

        equipmentRepository.save(equipment);
        return mapCalibrationToDTO(calibrationRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<CalibrationRecordDTO> getCalibrations(Long equipmentId) {
        if (!equipmentRepository.existsById(equipmentId))
            throw new EntityNotFoundException("Equipment not found: " + equipmentId);
        return calibrationRepository.findByEquipmentIdOrderByPerformedAtDesc(equipmentId)
                .stream().map(this::mapCalibrationToDTO).toList();
    }

    // ── MAINTENANCE ───────────────────────────────────────────────────────────

    @Transactional
    public MaintenanceRecordDTO addMaintenance(Long equipmentId, MaintenanceRecordDTO dto) {
        Equipment equipment = loadEquipment(equipmentId);

        User performedBy = dto.performedBy() != null
                ? userRepository.findByUsername(dto.performedBy()).orElse(null)
                : null;

        MaintenanceRecord record = MaintenanceRecord.builder()
                .equipment(equipment)
                .type(dto.type())
                .description(dto.description())
                .resolution(dto.resolution())
                .cost(dto.cost())
                .performedBy(performedBy)
                .externalProvider(dto.externalProvider())
                .build();

        return mapMaintenanceToDTO(maintenanceRepository.save(record));
    }

    @Transactional(readOnly = true)
    public List<MaintenanceRecordDTO> getMaintenanceRecords(Long equipmentId) {
        if (!equipmentRepository.existsById(equipmentId))
            throw new EntityNotFoundException("Equipment not found: " + equipmentId);
        return maintenanceRepository.findByEquipmentIdOrderByPerformedAtDesc(equipmentId)
                .stream().map(this::mapMaintenanceToDTO).toList();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private Equipment loadEquipment(Long id) {
        return equipmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Equipment not found: " + id));
    }

    // ── MAPPERS ───────────────────────────────────────────────────────────────

    private EquipmentDTO mapEquipmentToDTO(Equipment e) {
        return new EquipmentDTO(
                e.getId(), e.getName(), e.getModel(), e.getSerialNumber(),
                e.getManufacturer(), e.getType(), e.getLocation(), e.getStatus(),
                e.getPurchasedAt(), e.getNextCalibrationDue(), e.getNextMaintenanceDue(),
                e.getRegisteredAt()
        );
    }

    private CalibrationRecordDTO mapCalibrationToDTO(CalibrationRecord r) {
        return new CalibrationRecordDTO(
                r.getId(), r.getPerformedAt(), r.getNextDueAt(), r.getResult(),
                r.getPerformedBy() != null ? r.getPerformedBy().getUsername() : null,
                r.getExternalProvider(), r.getCertificateReference(), r.getObservations()
        );
    }

    private MaintenanceRecordDTO mapMaintenanceToDTO(MaintenanceRecord r) {
        return new MaintenanceRecordDTO(
                r.getId(), r.getType(), r.getPerformedAt(), r.getDescription(),
                r.getResolution(), r.getCost(),
                r.getPerformedBy() != null ? r.getPerformedBy().getUsername() : null,
                r.getExternalProvider()
        );
    }
}
