package com.kruzvinicius.limsbackend.model;

import com.kruzvinicius.limsbackend.model.enums.ServiceOrderPriority;
import com.kruzvinicius.limsbackend.model.enums.ServiceOrderStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Service Order (OS) — the central operational unit in the lab.
 * Groups samples, assigns analysts, tracks SLA deadlines.
 */
@Entity
@Audited
@Table(name = "service_orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Auto-generated sequential order number (e.g., "OS-2026-0001"). */
    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    private String description;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceOrderStatus status = ServiceOrderStatus.CREATED;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ServiceOrderPriority priority = ServiceOrderPriority.NORMAL;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);

    /** SLA deadline — when the order should be completed by. */
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    /** Customer who requested the analysis. */
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** User who created/registered the OS. */
    @ManyToOne
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /** Analyst assigned to execute the analyses. */
    @ManyToOne
    @JoinColumn(name = "assigned_to")
    private User assignedTo;

    /** Samples linked to this service order. */
    @Builder.Default
    @OneToMany(mappedBy = "serviceOrder", cascade = CascadeType.ALL)
    private List<Sample> samples = new ArrayList<>();
}
