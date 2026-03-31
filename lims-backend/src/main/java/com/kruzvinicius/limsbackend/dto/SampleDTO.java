package com.kruzvinicius.limsbackend.dto;

import com.kruzvinicius.limsbackend.model.Sample;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SampleDTO {
    private Long id;

    @NotBlank(message = "Barcode cannot be blank")
    @Size(min = 5, max = 20, message = "Barcode must be between 5 and 20 characters")
    private String barcode;

    @NotNull(message = "Material type must be provided")
    private String materialType;
    private String status;
    private String customerName;
    private String receivedAt;

    public static SampleDTO fromEntity(Sample sample) {
        return new SampleDTO(
                sample.getId(),
                sample.getBarcode(),
                sample.getMaterialType(),
                sample.getStatus() != null ? sample.getStatus().name() : null,
                sample.getCustomer() != null ? sample.getCustomer().getCorporateReason() : "N/A",
                sample.getReceivedAt() != null ? sample.getReceivedAt().toString() : "not Registered"
        );
    }
}