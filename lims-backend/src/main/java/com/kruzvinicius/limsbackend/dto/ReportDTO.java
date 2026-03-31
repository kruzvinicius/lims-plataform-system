package com.kruzvinicius.limsbackend.dto;

import java.util.Map;

/**
 * DTO for dashboard/report data.
 * Each report method returns a different shape of data using this flexible container.
 */
public record ReportDTO(
        /** Human-readable title for this report section. */
        String title,

        /** Key-value pairs with metric names and their values. */
        Map<String, Object> metrics
) {}
