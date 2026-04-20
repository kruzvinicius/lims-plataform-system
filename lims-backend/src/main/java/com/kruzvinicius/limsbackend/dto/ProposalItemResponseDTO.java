package com.kruzvinicius.limsbackend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProposalItemResponseDTO {
    private Long id;
    private String description;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
    private Long analysisTypeId;
    private String analysisTypeCode;
}
