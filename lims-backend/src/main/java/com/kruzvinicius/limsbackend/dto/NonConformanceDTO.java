package com.kruzvinicius.limsbackend.dto;

import com.kruzvinicius.limsbackend.model.enums.NonConformanceSeverity;
import com.kruzvinicius.limsbackend.model.enums.NonConformanceStatus;
import com.kruzvinicius.limsbackend.model.enums.NonConformanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public record NonConformanceDTO(
        Long id,

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Description is required")
        String description,

        @NotNull(message = "Type is required")
        NonConformanceType type,

        @NotNull(message = "Severity is required")
        NonConformanceSeverity severity,

        NonConformanceStatus status,

        OffsetDateTime detectedAt,
        OffsetDateTime resolvedAt,

        String rootCause,
        String correctiveAction,
        String preventiveAction,

        /** Username of the person who detected the NC. */
        String detectedBy,

        /** Username of the person assigned to investigate. */
        String assignedTo,

        /** ID of the related sample (optional). */
        Long sampleId,

        /** ID of the related test result (optional). */
        Long testResultId
) {}
