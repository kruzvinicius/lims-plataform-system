package com.kruzvinicius.limsbackend.dto;

/**
 * Using Record for consistency with SampleRequest and SampleResponse.
 * Automatically generates immutable fields and accessor methods.
 */
public record TestResultDTO(
        Long id,
        String parameterName,
        String resultValue,
        String unit,
        String performedAt
) {}