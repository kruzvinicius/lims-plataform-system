package com.kruzvinicius.limsbackend.repository;

import com.kruzvinicius.limsbackend.model.EnvironmentalLegislation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LegislationRepository extends JpaRepository<EnvironmentalLegislation, Long> {
    List<EnvironmentalLegislation> findByActiveTrueOrderByCodeAsc();
    List<EnvironmentalLegislation> findAllByOrderByCodeAsc();
    boolean existsByCode(String code);
}
