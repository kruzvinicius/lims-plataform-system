package com.kruzvinicius.limsbackend.dto;

import com.kruzvinicius.limsbackend.model.enums.CustodyEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

/**
 * DTO for chain-of-custody events.
 * Used both for creating events (request) and returning them (response).
 */
public record CustodyEventDTO(
        Long id,

        @NotNull(message = "Event type is required")
        CustodyEventType eventType,

        @NotBlank(message = "Location is required")
        String location,

        String notes,

        OffsetDateTime occurredAt,

        /** Username of the person who performed the transfer. */
        String transferredBy
) {}
