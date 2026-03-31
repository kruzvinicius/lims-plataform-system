package com.kruzvinicius.limsbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SampleRequest(
        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Barcode is required")
        String barcode,

        @NotBlank(message = "Material type is required")
        String materialType,

        /** Physical location where the sample was collected (optional). */
        String collectionLocation,

        /** Date the sample was collected (optional). */
        LocalDate collectionDate,

        /** Additional notes about the sample (optional). */
        String notes,

        @NotNull(message = "Customer ID is required")
        Long customerId
) {}
