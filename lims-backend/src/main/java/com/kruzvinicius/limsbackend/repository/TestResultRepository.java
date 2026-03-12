package com.kruzvinicius.limsbackend.repository;

import com.kruzvinicius.limsbackend.model.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestResultRepository extends JpaRepository<TestResult, Long> {
    List<TestResult> findBySampleId(Long sampleId);
}
