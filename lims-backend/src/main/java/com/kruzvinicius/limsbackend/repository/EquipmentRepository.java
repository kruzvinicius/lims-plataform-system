package com.kruzvinicius.limsbackend.repository;

import com.kruzvinicius.limsbackend.model.Equipment;
import com.kruzvinicius.limsbackend.model.enums.EquipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findByStatus(EquipmentStatus status);

    /** Returns equipment whose next calibration is overdue or within {@code days} days. */
    @Query("SELECT e FROM Equipment e WHERE e.nextCalibrationDue IS NOT NULL AND e.nextCalibrationDue <= :threshold")
    List<Equipment> findDueForCalibration(@Param("threshold") LocalDate threshold);

    /** Returns equipment whose next maintenance is overdue or within {@code days} days. */
    @Query("SELECT e FROM Equipment e WHERE e.nextMaintenanceDue IS NOT NULL AND e.nextMaintenanceDue <= :threshold")
    List<Equipment> findDueForMaintenance(@Param("threshold") LocalDate threshold);
}
