package com.kruzvinicius.limsbackend.dto;

import com.kruzvinicius.limsbackend.model.enums.ServiceOrderPriority;
import com.kruzvinicius.limsbackend.model.enums.ServiceOrderStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record ServiceOrderDTO(
        Long id,
        String orderNumber,

        @NotBlank(message = "Description is required")
        String description,

        ServiceOrderStatus status,
        ServiceOrderPriority priority,

        OffsetDateTime createdAt,
        LocalDate dueDate,
        OffsetDateTime completedAt,
        OffsetDateTime cancelledAt,
        String cancellationReason,

        @NotNull(message = "Customer ID is required")
        Long customerId,

        /** Username of the user who created the OS. */
        String createdBy,

        /** Username of the assigned analyst. */
        String assignedTo,

        /** IDs of samples linked to this OS. */
        List<Long> sampleIds
) {}
