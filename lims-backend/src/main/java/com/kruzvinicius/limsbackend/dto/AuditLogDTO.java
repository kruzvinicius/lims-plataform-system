package com.kruzvinicius.limsbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuditLogDTO {
    private Long revisionId;
    private String modifiedBy;
    private String timestamp;
    private String action;
    private String status;
}
