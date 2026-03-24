package com.kruzvinicius.limsbackend.dto;

public record CustomerResponse(
        Long id,
        String corporateReason,
        String email,
        String taxId,
        String phone
) {}

