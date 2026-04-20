package com.kruzvinicius.limsbackend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class ProposalResponseDTO {
    private Long id;
    private String proposalNumber;
    private String title;
    private String status;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private BigDecimal finalAmount;
    private OffsetDateTime createdAt;
    private LocalDate validUntil;
    private String customerName;
    private Long customerId;
    private String createdBy;
    private Long serviceOrderId;
    private Long legislationId;
    private String legislationName;
    private List<ProposalItemResponseDTO> items;
}
