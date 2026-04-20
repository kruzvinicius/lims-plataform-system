package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.dto.ProposalRequestDTO;
import com.kruzvinicius.limsbackend.dto.ProposalResponseDTO;
import com.kruzvinicius.limsbackend.service.ProposalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proposals")
@Slf4j
@RequiredArgsConstructor
public class CommercialProposalController {

    private final ProposalService proposalService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<ProposalResponseDTO>> getAllProposals() {
        return ResponseEntity.ok(proposalService.findAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ProposalResponseDTO> createProposal(@Valid @RequestBody ProposalRequestDTO request, Authentication authentication) {
        log.info("Creating commercial proposal by {}", authentication.getName());
        ProposalResponseDTO response = proposalService.createProposal(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ProposalResponseDTO> approveProposal(@PathVariable Long id, Authentication authentication) {
        log.info("Approving commercial proposal {} by {}", id, authentication.getName());
        ProposalResponseDTO response = proposalService.approveProposal(id, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ProposalResponseDTO> updateProposal(
            @PathVariable Long id,
            @Valid @RequestBody ProposalRequestDTO request,
            Authentication authentication) {
        log.info("Updating proposal {} by {}", id, authentication.getName());
        ProposalResponseDTO response = proposalService.updateProposal(id, request, authentication.getName());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> deleteProposal(@PathVariable Long id, Authentication authentication) {
        log.info("Deleting proposal {} by {}", id, authentication.getName());
        proposalService.deleteProposal(id);
        return ResponseEntity.noContent().build();
    }
}
