package com.kruzvinicius.limsbackend.repository;

import com.kruzvinicius.limsbackend.model.CalibrationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalibrationRecordRepository extends JpaRepository<CalibrationRecord, Long> {

    List<CalibrationRecord> findByEquipmentIdOrderByPerformedAtDesc(Long equipmentId);
}
