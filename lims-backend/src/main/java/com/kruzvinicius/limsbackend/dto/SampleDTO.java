package com.kruzvinicius.limsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SampleDTO {
    private Long id;
    private String barcode;
    private String materialType;
    private String status;
    private String customerName;
    private String receivedAt;
}
