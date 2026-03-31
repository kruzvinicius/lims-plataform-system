package com.kruzvinicius.limsbackend.repository;

import com.kruzvinicius.limsbackend.model.CustodyEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustodyEventRepository extends JpaRepository<CustodyEvent, Long> {

    /** Returns all custody events for a given sample, ordered chronologically. */
    List<CustodyEvent> findBySampleIdOrderByOccurredAtAsc(Long sampleId);
}
