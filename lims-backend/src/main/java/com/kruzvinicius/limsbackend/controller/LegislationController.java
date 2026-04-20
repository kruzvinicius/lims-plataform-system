package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.dto.LegislationDTO;
import com.kruzvinicius.limsbackend.service.LegislationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/legislations")
@Slf4j
@RequiredArgsConstructor
public class LegislationController {

    private final LegislationService legislationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LegislationDTO>> getAll() {
        return ResponseEntity.ok(legislationService.findAll());
    }

    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LegislationDTO>> getActive() {
        return ResponseEntity.ok(legislationService.findActive());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LegislationDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(legislationService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<LegislationDTO> create(@Valid @RequestBody LegislationDTO dto) {
        log.info("Creating legislation: {}", dto.code());
        return ResponseEntity.status(HttpStatus.CREATED).body(legislationService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<LegislationDTO> update(@PathVariable Long id, @Valid @RequestBody LegislationDTO dto) {
        log.info("Updating legislation: {}", id);
        return ResponseEntity.ok(legislationService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        log.info("Deleting legislation: {}", id);
        legislationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
