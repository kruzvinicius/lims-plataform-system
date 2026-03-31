package com.kruzvinicius.limsbackend.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record AnalysisTypeDTO(
        Long id,

        @NotBlank(message = "Code is required")
        String code,

        @NotBlank(message = "Name is required")
        String name,

        String description,
        String defaultUnit,

        BigDecimal minValue,
        BigDecimal maxValue,

        Boolean active
) {}
