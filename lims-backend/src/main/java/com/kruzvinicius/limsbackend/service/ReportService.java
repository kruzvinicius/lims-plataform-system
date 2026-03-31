package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.ReportDTO;
import com.kruzvinicius.limsbackend.model.enums.SampleStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Service for generating operational and management reports.
 * Uses native JPQL queries for analytics.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final EntityManager em;

    // ── SAMPLES OVERVIEW ──────────────────────────────────────────────────────

    /**
     * Summary of samples grouped by status.
     * Returns: { "RECEIVED": 15, "IN_ANALYSIS": 8, "PENDING_APPROVAL": 3, ... }
     */
    public ReportDTO samplesByStatus() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createQuery(
                "SELECT s.status, COUNT(s) FROM Sample s GROUP BY s.status").getResultList();

        Map<String, Object> metrics = new LinkedHashMap<>();
        long total = 0;
        for (Object[] row : rows) {
            SampleStatus st = (SampleStatus) row[0];
            Long count = (Long) row[1];
            metrics.put(st.name(), count);
            total += count;
        }
        metrics.put("TOTAL", total);

        return new ReportDTO("Amostras por Status", metrics);
    }

    /**
     * Count of pending samples (not yet RELEASED or REJECTED).
     */
    public ReportDTO pendingSamples() {
        Long count = em.createQuery(
                "SELECT COUNT(s) FROM Sample s WHERE s.status NOT IN (:terminal)", Long.class)
                .setParameter("terminal", List.of(SampleStatus.RELEASED, SampleStatus.REJECTED))
                .getSingleResult();

        return new ReportDTO("Amostras Pendentes", Map.of("pendentes", count));
    }

    // ── REJECTION RATE ────────────────────────────────────────────────────────

    /**
     * Rejection rate = rejected / total.
     */
    public ReportDTO rejectionRate() {
        Long total = em.createQuery("SELECT COUNT(s) FROM Sample s", Long.class).getSingleResult();
        Long rejected = em.createQuery(
                "SELECT COUNT(s) FROM Sample s WHERE s.status = :status", Long.class)
                .setParameter("status", SampleStatus.REJECTED)
                .getSingleResult();

        double rate = total > 0 ? (double) rejected / total * 100 : 0;

        return new ReportDTO("Taxa de Rejeição", Map.of(
                "total", total,
                "rejeitadas", rejected,
                "taxa_percentual", Math.round(rate * 100.0) / 100.0
        ));
    }

    // ── ANALYST PERFORMANCE ───────────────────────────────────────────────────

    /**
     * Results approved per analyst (based on TestResult.approvedBy).
     */
    public ReportDTO performanceByAnalyst() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createQuery(
                "SELECT tr.approvedBy.username, COUNT(tr) FROM TestResult tr " +
                "WHERE tr.approvedBy IS NOT NULL GROUP BY tr.approvedBy.username " +
                "ORDER BY COUNT(tr) DESC").getResultList();

        Map<String, Object> metrics = new LinkedHashMap<>();
        for (Object[] row : rows) {
            metrics.put((String) row[0], row[1]);
        }
        return new ReportDTO("Desempenho por Analista (resultados aprovados)", metrics);
    }

    // ── PRODUCTIVITY BY SECTOR ────────────────────────────────────────────────

    /**
     * Samples processed per material type (proxy for lab sector).
     */
    public ReportDTO productivityBySector() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createQuery(
                "SELECT COALESCE(s.materialType, 'Não definido'), COUNT(s) FROM Sample s " +
                "GROUP BY s.materialType ORDER BY COUNT(s) DESC").getResultList();

        Map<String, Object> metrics = new LinkedHashMap<>();
        for (Object[] row : rows) {
            metrics.put((String) row[0], row[1]);
        }
        return new ReportDTO("Produtividade por Setor/Material", metrics);
    }

    // ── SERVICE ORDERS ────────────────────────────────────────────────────────

    /**
     * Service orders grouped by status.
     */
    public ReportDTO serviceOrdersByStatus() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createQuery(
                "SELECT so.status, COUNT(so) FROM ServiceOrder so GROUP BY so.status").getResultList();

        Map<String, Object> metrics = new LinkedHashMap<>();
        for (Object[] row : rows) {
            metrics.put(row[0].toString(), row[1]);
        }
        return new ReportDTO("Ordens de Serviço por Status", metrics);
    }

    /**
     * Average analysis time (time from RECEIVED to RELEASED) in hours.
     */
    public ReportDTO averageAnalysisTime() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createNativeQuery(
                "SELECT AVG(EXTRACT(EPOCH FROM (so.completed_at - so.created_at))/3600) as avg_hours, " +
                "COUNT(*) as completed_count " +
                "FROM service_orders so WHERE so.status = 'COMPLETED' AND so.completed_at IS NOT NULL")
                .getResultList();

        Map<String, Object> metrics = new LinkedHashMap<>();
        if (!rows.isEmpty()) {
            Object[] row = rows.get(0);
            double avgHours = row[0] != null ? ((Number) row[0]).doubleValue() : 0;
            metrics.put("tempo_medio_horas", Math.round(avgHours * 100.0) / 100.0);
            metrics.put("ordens_concluidas", row[1]);
        }
        return new ReportDTO("Tempo Médio de Análise (horas)", metrics);
    }

    // ── NON-CONFORMANCES ──────────────────────────────────────────────────────

    /**
     * Non-conformances grouped by status.
     */
    public ReportDTO nonConformancesByStatus() {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = em.createQuery(
                "SELECT nc.status, COUNT(nc) FROM NonConformance nc GROUP BY nc.status").getResultList();

        Map<String, Object> metrics = new LinkedHashMap<>();
        for (Object[] row : rows) {
            metrics.put(row[0].toString(), row[1]);
        }
        return new ReportDTO("Não Conformidades por Status", metrics);
    }

    // ── FULL DASHBOARD ────────────────────────────────────────────────────────

    /**
     * Aggregated dashboard with all key indicators.
     */
    public List<ReportDTO> dashboard() {
        return List.of(
                samplesByStatus(),
                pendingSamples(),
                rejectionRate(),
                performanceByAnalyst(),
                productivityBySector(),
                serviceOrdersByStatus(),
                averageAnalysisTime(),
                nonConformancesByStatus()
        );
    }
}
