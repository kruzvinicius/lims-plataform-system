package com.kruzvinicius.limsbackend.controller;

import com.kruzvinicius.limsbackend.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for data export.
 * Returns CSV files as downloadable attachments.
 */
@RestController
@RequestMapping("/api/export")
@Slf4j
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    /** GET /api/export/samples — download all samples as CSV */
    @GetMapping("/samples")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<byte[]> exportSamples() {
        log.info("REST request to export all samples as CSV");
        String csv = exportService.exportSamplesCSV();
        return buildCsvResponse(csv, "samples_export.csv");
    }

    /** GET /api/export/samples/{id}/results — download results for a sample as CSV */
    @GetMapping("/samples/{id}/results")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','ANALYST')")
    public ResponseEntity<byte[]> exportResults(@PathVariable Long id) {
        log.info("REST request to export results for sample {} as CSV", id);
        String csv = exportService.exportResultsCSV(id);
        return buildCsvResponse(csv, "results_sample_" + id + ".csv");
    }

    private ResponseEntity<byte[]> buildCsvResponse(String csvContent, String filename) {
        byte[] bytes = csvContent.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .contentLength(bytes.length)
                .body(bytes);
    }
}
