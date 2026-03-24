package com.kruzvinicius.limsbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SampleRequest(
        @NotBlank(message = "Description is required")
        String description,

        @NotBlank(message = "Barcode is required")
        String barcode,

        @NotBlank(message = "Material type is required")
        String materialType,

        @NotNull(message = "Customer ID is required")
        Long customerId
) {}
