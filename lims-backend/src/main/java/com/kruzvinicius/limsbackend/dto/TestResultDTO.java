package com.kruzvinicius.limsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TestResultDTO {
    private Long id;
    private String parameterName;
    private String resultValue;
    private String unit;
    private String performedAt;
}
