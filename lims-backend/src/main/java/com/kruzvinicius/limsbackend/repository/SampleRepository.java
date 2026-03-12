package com.kruzvinicius.limsbackend.repository;

import com.kruzvinicius.limsbackend.model.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SampleRepository extends JpaRepository<Sample, Long> {

    Optional<Sample> findByBarcode(String barcode);

    List<Sample> findByCustomerId(Long customerId);
}
