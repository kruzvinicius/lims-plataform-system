package com.kruzvinicius.limsbackend.repository;

import com.kruzvinicius.limsbackend.model.LegislationParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LegislationParameterRepository extends JpaRepository<LegislationParameter, Long> {
    List<LegislationParameter> findByLegislationId(Long legislationId);
    Optional<LegislationParameter> findByLegislationIdAndAnalysisTypeId(Long legislationId, Long analysisTypeId);
}
