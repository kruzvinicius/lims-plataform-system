package com.kruzvinicius.limsbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.math.BigDecimal;

@Entity
@Audited
@Table(name = "proposal_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProposalItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false)
    private CommercialProposal proposal;

    /** E.g., "Análise de Cloro Residual", "Taxa de Deslocamento", "ART" */
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "total_price", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    /** Optional link to a registered parameter in the Master Data. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_type_id")
    private AnalysisType analysisType;
}
