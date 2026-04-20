package com.kruzvinicius.limsbackend.repository;

import com.kruzvinicius.limsbackend.model.ProposalItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProposalItemRepository extends JpaRepository<ProposalItem, Long> {
    List<ProposalItem> findByProposalId(Long proposalId);
}
