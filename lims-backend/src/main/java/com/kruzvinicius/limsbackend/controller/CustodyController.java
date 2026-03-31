package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.dto.CustodyEventDTO;
import com.kruzvinicius.limsbackend.service.CustodyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Chain-of-Custody management.
 * Records every physical transfer or key event involving a sample.
 */
@RestController
@RequestMapping("/api/samples/{sampleId}/custody")
@Slf4j
@RequiredArgsConstructor
public class CustodyController {

    private final CustodyService custodyService;

    /** POST /api/samples/{sampleId}/custody — register custody event */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<CustodyEventDTO> registerEvent(
            @PathVariable Long sampleId,
            @Valid @RequestBody CustodyEventDTO dto) {
        log.info("REST request to register custody event {} for Sample : {}", dto.eventType(), sampleId);
        CustodyEventDTO saved = custodyService.registerEvent(sampleId, dto, dto.transferredBy());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /** GET /api/samples/{sampleId}/custody — full chain of custody */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<List<CustodyEventDTO>> getChain(@PathVariable Long sampleId) {
        log.info("REST request to get custody chain for Sample : {}", sampleId);
        return ResponseEntity.ok(custodyService.getChain(sampleId));
    }
}
