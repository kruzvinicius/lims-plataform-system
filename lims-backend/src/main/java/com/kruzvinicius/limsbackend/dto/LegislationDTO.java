package com.kruzvinicius.limsbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record LegislationDTO(
        Long id,

        @NotBlank(message = "Code is required")
        String code,

        @NotBlank(message = "Name is required")
        String name,

        String region,
        String description,
        Boolean active,

        /** VMP definitions for each parameter covered by this legislation. */
        List<LegislationParameterDTO> parameters
) {}
