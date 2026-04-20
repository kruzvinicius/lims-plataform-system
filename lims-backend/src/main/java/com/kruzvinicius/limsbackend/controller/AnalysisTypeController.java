package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.dto.AnalysisTypeDTO;
import com.kruzvinicius.limsbackend.service.AnalysisTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Analysis Type definitions.
 * Manages test types with acceptance ranges for automatic result validation.
 */
@RestController
@RequestMapping("/api/analysis-types")
@Slf4j
@RequiredArgsConstructor
public class AnalysisTypeController {

    private final AnalysisTypeService service;

    /** POST /api/analysis-types — register a new analysis type */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<AnalysisTypeDTO> create(@Valid @RequestBody AnalysisTypeDTO dto) {
        log.info("REST request to create AnalysisType: {}", dto.code());
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(dto));
    }

    /** PUT /api/analysis-types/{id} — update an analysis type */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<AnalysisTypeDTO> update(@PathVariable Long id, @Valid @RequestBody AnalysisTypeDTO dto) {
        log.info("REST request to update AnalysisType: {}", id);
        return ResponseEntity.ok(service.update(id, dto));
    }

    /** GET /api/analysis-types — list all */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<List<AnalysisTypeDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    /** GET /api/analysis-types/active — list only active types */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<List<AnalysisTypeDTO>> getActive() {
        return ResponseEntity.ok(service.findActive());
    }

    /** GET /api/analysis-types/{id} — get by ID */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<AnalysisTypeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    /** DELETE /api/analysis-types/{id} — remove an analysis type */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("REST request to delete AnalysisType: {}", id);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
