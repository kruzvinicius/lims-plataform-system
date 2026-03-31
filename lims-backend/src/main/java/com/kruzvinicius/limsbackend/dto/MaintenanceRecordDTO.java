package com.kruzvinicius.limsbackend.dto;

import com.kruzvinicius.limsbackend.model.enums.MaintenanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record MaintenanceRecordDTO(
        Long id,

        @NotNull(message = "Type is required")
        MaintenanceType type,

        OffsetDateTime performedAt,

        @NotBlank(message = "Description is required")
        String description,

        String resolution,
        BigDecimal cost,

        /** Username of the internal user who performed maintenance (optional). */
        String performedBy,

        String externalProvider
) {}
