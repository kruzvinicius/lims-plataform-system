package com.kruzvinicius.limsbackend.repository;

import com.kruzvinicius.limsbackend.model.CommercialProposal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommercialProposalRepository extends JpaRepository<CommercialProposal, Long> {
    Optional<CommercialProposal> findByProposalNumber(String proposalNumber);
    List<CommercialProposal> findByCustomerId(Long customerId);
    List<CommercialProposal> findAllByOrderByCreatedAtDesc();
}
