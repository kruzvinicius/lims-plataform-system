package com.kruzvinicius.limsbackend.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.OffsetDateTime;

/**
 * DTO for reanalysis requests.
 * Used when requesting reanalysis after rejection and for returning request details.
 */
public record ReanalysisRequestDTO(
        Long id,

        @NotBlank(message = "Reason is required")
        String reason,

        OffsetDateTime requestedAt,

        String requestedBy,

        OffsetDateTime resolvedAt,

        String resolutionNotes
) {}
