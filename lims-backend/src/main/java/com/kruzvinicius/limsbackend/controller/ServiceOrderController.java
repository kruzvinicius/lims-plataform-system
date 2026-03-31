package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.dto.ServiceOrderDTO;
import com.kruzvinicius.limsbackend.model.enums.ServiceOrderStatus;
import com.kruzvinicius.limsbackend.service.ServiceOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Service Order management.
 * Full lifecycle: CREATED → ASSIGNED → IN_PROGRESS → COMPLETED | CANCELLED.
 */
@RestController
@RequestMapping("/api/service-orders")
@Slf4j
@RequiredArgsConstructor
public class ServiceOrderController {

    private final ServiceOrderService soService;

    /** POST /api/service-orders — create a new service order */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<ServiceOrderDTO> create(
            @Valid @RequestBody ServiceOrderDTO dto,
            Authentication authentication) {
        log.info("REST request to create ServiceOrder by {}", authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(soService.create(dto, authentication.getName()));
    }

    /** GET /api/service-orders — list all, optionally filtered by status */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<List<ServiceOrderDTO>> getAll(
            @RequestParam(required = false) ServiceOrderStatus status) {
        return ResponseEntity.ok(soService.findAll(status));
    }

    /** GET /api/service-orders/{id} — get by ID */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST','TECHNICIAN')")
    public ResponseEntity<ServiceOrderDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(soService.findById(id));
    }

    /** PATCH /api/service-orders/{id}/assign?username=... — assign an analyst */
    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ServiceOrderDTO> assign(
            @PathVariable Long id, @RequestParam String username) {
        log.info("REST request to assign SO {} to {}", id, username);
        return ResponseEntity.ok(soService.assign(id, username));
    }

    /** PATCH /api/service-orders/{id}/start — start working */
    @PatchMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST')")
    public ResponseEntity<ServiceOrderDTO> start(@PathVariable Long id) {
        return ResponseEntity.ok(soService.start(id));
    }

    /** PATCH /api/service-orders/{id}/complete — mark as completed */
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST')")
    public ResponseEntity<ServiceOrderDTO> complete(@PathVariable Long id) {
        return ResponseEntity.ok(soService.complete(id));
    }

    /** PATCH /api/service-orders/{id}/cancel — cancel with reason */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ServiceOrderDTO> cancel(
            @PathVariable Long id, @RequestBody String reason) {
        log.info("REST request to cancel SO {}", id);
        return ResponseEntity.ok(soService.cancel(id, reason));
    }

    /** GET /api/service-orders/pending — queue of pending orders */
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST')")
    public ResponseEntity<List<ServiceOrderDTO>> pending() {
        return ResponseEntity.ok(soService.findPending());
    }

    /** GET /api/service-orders/overdue — orders with expired SLA */
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<ServiceOrderDTO>> overdue() {
        return ResponseEntity.ok(soService.findOverdue());
    }

    /** GET /api/service-orders/analyst/{username} — tasks for a specific analyst */
    @GetMapping("/analyst/{username}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER') or #username == authentication.name")
    public ResponseEntity<List<ServiceOrderDTO>> byAnalyst(@PathVariable String username) {
        return ResponseEntity.ok(soService.findByAnalyst(username));
    }
}
