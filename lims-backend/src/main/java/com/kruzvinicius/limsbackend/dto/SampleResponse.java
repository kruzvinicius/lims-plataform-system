package com.kruzvinicius.limsbackend.dto;

import java.time.OffsetDateTime;

public record SampleResponse(
        Long id,
        String description,
        String barcode,
        String materialType,
        String collectionLocation,
        java.time.LocalDate collectionDate,
        String notes,
        String status,
        String rejectionReason,
        Long customerId,
        OffsetDateTime receivedAt) {
}
