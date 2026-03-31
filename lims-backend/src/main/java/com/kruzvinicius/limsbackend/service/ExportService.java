package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.model.Sample;
import com.kruzvinicius.limsbackend.model.TestResult;
import com.kruzvinicius.limsbackend.repository.SampleRepository;
import com.kruzvinicius.limsbackend.repository.TestResultRepository;
import com.opencsv.CSVWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

/**
 * Service for exporting lab data to CSV format.
 * Returns CSV string content; the controller handles the HTTP response.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExportService {

    private final SampleRepository sampleRepository;
    private final TestResultRepository testResultRepository;

    /**
     * Export all samples to CSV.
     */
    @Transactional(readOnly = true)
    public String exportSamplesCSV() {
        List<Sample> samples = sampleRepository.findAll();

        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            // Header
            writer.writeNext(new String[]{
                    "ID", "Barcode", "Description", "Material Type", "Status",
                    "Collection Location", "Collection Date", "Customer ID",
                    "Received At", "Rejection Reason"
            });

            // Data
            for (Sample s : samples) {
                writer.writeNext(new String[]{
                        str(s.getId()),
                        s.getBarcode(),
                        s.getDescription(),
                        s.getMaterialType(),
                        s.getStatus() != null ? s.getStatus().name() : "",
                        s.getCollectionLocation(),
                        str(s.getCollectionDate()),
                        str(s.getCustomer() != null ? s.getCustomer().getId() : null),
                        str(s.getReceivedAt()),
                        s.getRejectionReason() != null ? s.getRejectionReason() : ""
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV", e);
        }

        log.info("Exported {} samples to CSV", samples.size());
        return sw.toString();
    }

    /**
     * Export test results for a specific sample to CSV.
     */
    @Transactional(readOnly = true)
    public String exportResultsCSV(Long sampleId) {
        List<TestResult> results = testResultRepository.findBySampleId(sampleId);

        StringWriter sw = new StringWriter();
        try (CSVWriter writer = new CSVWriter(sw)) {
            writer.writeNext(new String[]{
                    "ID", "Parameter", "Result Value", "Unit", "Status",
                    "Performed At", "Approved By", "Approved At", "Rejection Reason"
            });

            for (TestResult r : results) {
                writer.writeNext(new String[]{
                        str(r.getId()),
                        r.getParameterName(),
                        r.getResultValue(),
                        r.getUnit(),
                        r.getStatus() != null ? r.getStatus().name() : "",
                        str(r.getPerformedAt()),
                        r.getApprovedBy() != null ? r.getApprovedBy().getUsername() : "",
                        str(r.getApprovedAt()),
                        r.getRejectionReason() != null ? r.getRejectionReason() : ""
                });
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate CSV", e);
        }

        log.info("Exported {} results for sample {} to CSV", results.size(), sampleId);
        return sw.toString();
    }

    private String str(Object obj) {
        return obj != null ? obj.toString() : "";
    }
}
