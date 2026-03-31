package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.model.Customer;
import com.kruzvinicius.limsbackend.model.Sample;
import com.kruzvinicius.limsbackend.model.TestResult;
import com.kruzvinicius.limsbackend.model.enums.SampleStatus;
import com.kruzvinicius.limsbackend.model.enums.TestResultStatus;
import com.kruzvinicius.limsbackend.repository.SampleRepository;
import com.kruzvinicius.limsbackend.repository.TestResultRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest {

    @Mock private SampleRepository sampleRepository;
    @Mock private TestResultRepository testResultRepository;

    @InjectMocks private ExportService exportService;

    @Test
    @DisplayName("exportSamplesCSV should return CSV with header and data rows")
    void shouldExportSamples() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setCorporateReason("Lab Central");

        Sample sample = new Sample();
        sample.setId(1L);
        sample.setBarcode("BC-001");
        sample.setDescription("Water sample");
        sample.setMaterialType("Water");
        sample.setStatus(SampleStatus.RECEIVED);
        sample.setCustomer(customer);

        when(sampleRepository.findAll()).thenReturn(List.of(sample));

        String csv = exportService.exportSamplesCSV();

        assertThat(csv).contains("ID", "Barcode", "Description", "Material Type", "Status");
        assertThat(csv).contains("BC-001");
        assertThat(csv).contains("Water sample");
        assertThat(csv).contains("RECEIVED");
        // CSV should have header + 1 data row
        String[] lines = csv.trim().split("\n");
        assertThat(lines).hasSize(2);
    }

    @Test
    @DisplayName("exportSamplesCSV should return only header when no samples")
    void shouldExportEmptySamples() {
        when(sampleRepository.findAll()).thenReturn(List.of());

        String csv = exportService.exportSamplesCSV();

        assertThat(csv).contains("ID");
        String[] lines = csv.trim().split("\n");
        assertThat(lines).hasSize(1); // header only
    }

    @Test
    @DisplayName("exportResultsCSV should include test result details")
    void shouldExportResults() {
        TestResult result = new TestResult();
        result.setId(1L);
        result.setParameterName("pH");
        result.setResultValue("7.2");
        result.setUnit("pH units");
        result.setStatus(TestResultStatus.APPROVED);

        when(testResultRepository.findBySampleId(1L)).thenReturn(List.of(result));

        String csv = exportService.exportResultsCSV(1L);

        assertThat(csv).contains("Parameter", "Result Value", "Unit", "Status");
        assertThat(csv).contains("pH");
        assertThat(csv).contains("7.2");
        assertThat(csv).contains("APPROVED");
    }
}
