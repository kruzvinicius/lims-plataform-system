package com.kruzvinicius.limsbackend.dto;

import com.kruzvinicius.limsbackend.model.enums.CalibrationResult;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record CalibrationRecordDTO(
        Long id,
        OffsetDateTime performedAt,
        LocalDate nextDueAt,

        @NotNull(message = "Result is required")
        CalibrationResult result,

        /** Username of the internal user who performed calibration (optional). */
        String performedBy,

        String externalProvider,
        String certificateReference,
        String observations
) {}
