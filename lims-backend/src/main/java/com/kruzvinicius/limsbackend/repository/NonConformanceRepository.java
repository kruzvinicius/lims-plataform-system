package com.kruzvinicius.limsbackend.repository;

import com.kruzvinicius.limsbackend.model.NonConformance;
import com.kruzvinicius.limsbackend.model.enums.NonConformanceSeverity;
import com.kruzvinicius.limsbackend.model.enums.NonConformanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NonConformanceRepository extends JpaRepository<NonConformance, Long> {

    List<NonConformance> findBySampleId(Long sampleId);

    List<NonConformance> findByStatus(NonConformanceStatus status);

    List<NonConformance> findBySeverity(NonConformanceSeverity severity);

    List<NonConformance> findByStatusAndSeverity(NonConformanceStatus status, NonConformanceSeverity severity);

    List<NonConformance> findByAssignedToUsername(String username);
}
