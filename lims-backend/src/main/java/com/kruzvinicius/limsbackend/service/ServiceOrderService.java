package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.ServiceOrderDTO;
import com.kruzvinicius.limsbackend.dto.exception.EntityNotFoundException;
import com.kruzvinicius.limsbackend.model.*;
import com.kruzvinicius.limsbackend.model.enums.ServiceOrderPriority;
import com.kruzvinicius.limsbackend.model.enums.ServiceOrderStatus;
import com.kruzvinicius.limsbackend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Service for managing the Service Order lifecycle:
 * CREATED → ASSIGNED → IN_PROGRESS → COMPLETED (or CANCELLED at any point).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServiceOrderService {

    private final ServiceOrderRepository soRepository;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final SampleRepository sampleRepository;

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public ServiceOrderDTO create(ServiceOrderDTO dto, String createdByUsername) {
        Customer customer = customerRepository.findById(dto.customerId())
                .orElseThrow(() -> new EntityNotFoundException("Customer not found: " + dto.customerId()));

        User createdBy = loadUser(createdByUsername);

        ServiceOrder order = ServiceOrder.builder()
                .orderNumber(generateOrderNumber())
                .description(dto.description())
                .priority(dto.priority() != null ? dto.priority() : ServiceOrderPriority.NORMAL)
                .dueDate(dto.dueDate())
                .customer(customer)
                .createdBy(createdBy)
                .build();

        order = soRepository.save(order);

        // Link samples if provided
        if (dto.sampleIds() != null && !dto.sampleIds().isEmpty()) {
            for (Long sampleId : dto.sampleIds()) {
                Sample sample = sampleRepository.findById(sampleId)
                        .orElseThrow(() -> new EntityNotFoundException("Sample not found: " + sampleId));
                sample.setServiceOrder(order);
                sampleRepository.save(sample);
            }
        }

        log.info("Service Order {} created by {}", order.getOrderNumber(), createdByUsername);
        return mapToDTO(soRepository.findById(order.getId()).orElseThrow());
    }

    // ── WORKFLOW ──────────────────────────────────────────────────────────────

    @Transactional
    public ServiceOrderDTO assign(Long id, String assigneeUsername) {
        ServiceOrder order = loadOrder(id);
        validateTransition(order.getStatus(), ServiceOrderStatus.ASSIGNED);
        User assignee = loadUser(assigneeUsername);
        order.setAssignedTo(assignee);
        order.setStatus(ServiceOrderStatus.ASSIGNED);
        log.info("Service Order {} assigned to {}", order.getOrderNumber(), assigneeUsername);
        return mapToDTO(soRepository.save(order));
    }

    @Transactional
    public ServiceOrderDTO start(Long id) {
        ServiceOrder order = loadOrder(id);
        validateTransition(order.getStatus(), ServiceOrderStatus.IN_PROGRESS);
        order.setStatus(ServiceOrderStatus.IN_PROGRESS);
        log.info("Service Order {} started", order.getOrderNumber());
        return mapToDTO(soRepository.save(order));
    }

    @Transactional
    public ServiceOrderDTO complete(Long id) {
        ServiceOrder order = loadOrder(id);
        validateTransition(order.getStatus(), ServiceOrderStatus.COMPLETED);
        order.setStatus(ServiceOrderStatus.COMPLETED);
        order.setCompletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        log.info("Service Order {} completed", order.getOrderNumber());
        return mapToDTO(soRepository.save(order));
    }

    @Transactional
    public ServiceOrderDTO cancel(Long id, String reason) {
        ServiceOrder order = loadOrder(id);
        if (order.getStatus() == ServiceOrderStatus.COMPLETED || order.getStatus() == ServiceOrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot cancel an order that is already " + order.getStatus());
        }
        order.setStatus(ServiceOrderStatus.CANCELLED);
        order.setCancelledAt(OffsetDateTime.now(ZoneOffset.UTC));
        order.setCancellationReason(reason);
        log.info("Service Order {} cancelled: {}", order.getOrderNumber(), reason);
        return mapToDTO(soRepository.save(order));
    }

    // ── QUERIES ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ServiceOrderDTO findById(Long id) {
        return mapToDTO(loadOrder(id));
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderDTO> findAll(ServiceOrderStatus status) {
        List<ServiceOrder> orders = status != null
                ? soRepository.findByStatus(status)
                : soRepository.findAll();
        return orders.stream().map(this::mapToDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderDTO> findPending() {
        return soRepository.findPending().stream().map(this::mapToDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderDTO> findOverdue() {
        return soRepository.findOverdue(LocalDate.now()).stream().map(this::mapToDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceOrderDTO> findByAnalyst(String username) {
        return soRepository.findByAssignedToUsername(username).stream().map(this::mapToDTO).toList();
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private ServiceOrder loadOrder(Long id) {
        return soRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Service Order not found: " + id));
    }

    private User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    private String generateOrderNumber() {
        Long maxId = soRepository.findMaxId();
        return String.format("OS-%d-%04d", Year.now().getValue(), maxId + 1);
    }

    private void validateTransition(ServiceOrderStatus from, ServiceOrderStatus to) {
        boolean valid = switch (to) {
            case ASSIGNED -> from == ServiceOrderStatus.CREATED;
            case IN_PROGRESS -> from == ServiceOrderStatus.ASSIGNED;
            case COMPLETED -> from == ServiceOrderStatus.IN_PROGRESS;
            default -> false;
        };
        if (!valid) {
            throw new IllegalStateException("Cannot transition from " + from + " to " + to);
        }
    }

    // ── MAPPER ────────────────────────────────────────────────────────────────

    private ServiceOrderDTO mapToDTO(ServiceOrder o) {
        List<Long> sampleIds = o.getSamples() != null
                ? o.getSamples().stream().map(Sample::getId).toList()
                : List.of();

        return new ServiceOrderDTO(
                o.getId(), o.getOrderNumber(), o.getDescription(),
                o.getStatus(), o.getPriority(),
                o.getCreatedAt(), o.getDueDate(), o.getCompletedAt(),
                o.getCancelledAt(), o.getCancellationReason(),
                o.getCustomer().getId(),
                o.getCreatedBy() != null ? o.getCreatedBy().getUsername() : null,
                o.getAssignedTo() != null ? o.getAssignedTo().getUsername() : null,
                sampleIds
        );
    }
}
