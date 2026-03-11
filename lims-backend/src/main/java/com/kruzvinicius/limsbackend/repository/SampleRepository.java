package com.kruzvinicius.limsbackend.repository;

import com.kruzvinicius.limsbackend.model.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SampleRepository extends JpaRepository<Sample, Long> {
}
