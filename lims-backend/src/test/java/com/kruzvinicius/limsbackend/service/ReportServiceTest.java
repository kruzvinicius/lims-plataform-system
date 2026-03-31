package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.ReportDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private EntityManager em;
    @InjectMocks private ReportService reportService;

    @Test
    @DisplayName("samplesByStatus should return counts grouped by status")
    void samplesByStatusShouldGroup() {
        // Simulating JPQL result
        Query mockQuery = mock(Query.class);
        when(em.createQuery(contains("GROUP BY s.status"))).thenReturn(mockQuery);
        when(mockQuery.getResultList()).thenReturn(List.of(
                new Object[]{com.kruzvinicius.limsbackend.model.enums.SampleStatus.RECEIVED, 5L},
                new Object[]{com.kruzvinicius.limsbackend.model.enums.SampleStatus.IN_ANALYSIS, 3L}
        ));

        ReportDTO result = reportService.samplesByStatus();

        assertThat(result.title()).isEqualTo("Amostras por Status");
        assertThat(result.metrics()).containsEntry("RECEIVED", 5L);
        assertThat(result.metrics()).containsEntry("IN_ANALYSIS", 3L);
        assertThat(result.metrics()).containsEntry("TOTAL", 8L);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("pendingSamples should count non-terminal samples")
    void pendingShouldCount() {
        TypedQuery<Long> mockQuery = mock(TypedQuery.class);
        when(em.createQuery(contains("NOT IN"), eq(Long.class))).thenReturn(mockQuery);
        when(mockQuery.setParameter(eq("terminal"), any())).thenReturn(mockQuery);
        when(mockQuery.getSingleResult()).thenReturn(12L);

        ReportDTO result = reportService.pendingSamples();

        assertThat(result.metrics()).containsEntry("pendentes", 12L);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("rejectionRate should calculate percentage")
    void rejectionRateShouldCalculate() {
        TypedQuery<Long> totalQuery = mock(TypedQuery.class);
        TypedQuery<Long> rejectedQuery = mock(TypedQuery.class);

        when(em.createQuery("SELECT COUNT(s) FROM Sample s", Long.class)).thenReturn(totalQuery);
        when(totalQuery.getSingleResult()).thenReturn(100L);

        when(em.createQuery(contains("WHERE s.status"), eq(Long.class))).thenReturn(rejectedQuery);
        when(rejectedQuery.setParameter(eq("status"), any())).thenReturn(rejectedQuery);
        when(rejectedQuery.getSingleResult()).thenReturn(10L);

        ReportDTO result = reportService.rejectionRate();

        assertThat(result.title()).contains("Rejeição");
        assertThat(result.metrics()).containsEntry("total", 100L);
        assertThat(result.metrics()).containsEntry("rejeitadas", 10L);
        assertThat((double) result.metrics().get("taxa_percentual")).isEqualTo(10.0);
    }

    @Test
    @DisplayName("dashboard should return all 8 report sections")
    void dashboardShouldReturnAll() {
        // Setup minimal mocks for all queries
        Query genericQuery = mock(Query.class);
        when(genericQuery.getResultList()).thenReturn(List.of());

        @SuppressWarnings("unchecked")
        TypedQuery<Long> typedQuery = mock(TypedQuery.class);
        when(typedQuery.setParameter(anyString(), any())).thenReturn(typedQuery);
        when(typedQuery.getSingleResult()).thenReturn(0L);

        Query nativeQuery = mock(Query.class);
        when(nativeQuery.getResultList()).thenReturn(java.util.Collections.singletonList(new Object[]{0.0, 0L}));

        when(em.createQuery(anyString())).thenReturn(genericQuery);
        when(em.createQuery(anyString(), eq(Long.class))).thenReturn(typedQuery);
        when(em.createNativeQuery(anyString())).thenReturn(nativeQuery);

        List<ReportDTO> dashboard = reportService.dashboard();

        assertThat(dashboard).hasSize(8);
        assertThat(dashboard.stream().map(ReportDTO::title)).contains(
                "Amostras por Status",
                "Amostras Pendentes",
                "Taxa de Rejeição"
        );
    }
}
