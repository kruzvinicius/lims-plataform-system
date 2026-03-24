package com.kruzvinicius.limsbackend.dto;

import java.time.OffsetDateTime;

public record SampleResponse(
        Long id,
        String description,
        String barcode,
        String materialType,
        String status,
        Long customerId,
        OffsetDateTime receivedAt) {
}
