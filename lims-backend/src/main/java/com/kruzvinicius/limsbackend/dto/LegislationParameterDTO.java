package com.kruzvinicius.limsbackend.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record LegislationParameterDTO(
        Long id,

        @NotNull(message = "Analysis type ID is required")
        Long analysisTypeId,

        /** Analysis type code for display purposes (read-only from response). */
        String analysisTypeCode,
        String analysisTypeName,
        String analysisTypeUnit,

        /** Minimum permitted value (optional). Used for ranged parameters like pH. */
        BigDecimal vmpMin,

        @NotNull(message = "Maximum VMP is required")
        BigDecimal vmpMax,

        String unit,
        String notes
) {}
