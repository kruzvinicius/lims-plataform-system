package com.kruzvinicius.limsbackend.repository;

import com.kruzvinicius.limsbackend.model.ReanalysisRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReanalysisRequestRepository extends JpaRepository<ReanalysisRequest, Long> {

    /** Returns all reanalysis requests for a given sample, newest first. */
    List<ReanalysisRequest> findBySampleIdOrderByRequestedAtDesc(Long sampleId);
}
