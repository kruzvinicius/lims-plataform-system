package com.kruzvinicius.limsbackend.repository;

import com.kruzvinicius.limsbackend.model.AnalysisType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisTypeRepository extends JpaRepository<AnalysisType, Long> {

    Optional<AnalysisType> findByCode(String code);

    List<AnalysisType> findByActiveTrue();
}
