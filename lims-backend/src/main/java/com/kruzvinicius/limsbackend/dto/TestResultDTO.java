package com.kruzvinicius.limsbackend.dto;

/**
 * DTO for analytical test results.
 * Includes status and approval metadata.
 */
public record TestResultDTO(
        Long id,
        String parameterName,
        String resultValue,
        String unit,
        String performedAt,
        String status,
        String approvedBy,
        String approvedAt,
        String rejectionReason
) {}