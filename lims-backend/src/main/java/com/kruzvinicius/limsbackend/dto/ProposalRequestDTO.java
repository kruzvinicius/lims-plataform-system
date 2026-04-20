package com.kruzvinicius.limsbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProposalRequestDTO {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Title is required")
    private String title;

    private LocalDate validUntil;

    private Long legislationId;

    private List<ProposalItemRequestDTO> items;
}
