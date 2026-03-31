package com.kruzvinicius.limsbackend.dto;

import com.kruzvinicius.limsbackend.model.enums.EquipmentStatus;
import com.kruzvinicius.limsbackend.model.enums.EquipmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;

public record EquipmentDTO(
        Long id,

        @NotBlank(message = "Name is required")
        String name,

        String model,
        String serialNumber,
        String manufacturer,

        @NotNull(message = "Type is required")
        EquipmentType type,

        String location,
        EquipmentStatus status,

        LocalDate purchasedAt,
        LocalDate nextCalibrationDue,
        LocalDate nextMaintenanceDue,
        OffsetDateTime registeredAt
) {}
