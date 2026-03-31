package com.kruzvinicius.limsbackend.model;

import com.kruzvinicius.limsbackend.model.enums.SampleStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Entity
@Audited
@Table(name = "samples")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(unique = true, nullable = false)
    private String barcode;

    @Column(name = "material_type")
    private String materialType;

    /** Physical location where the sample was collected. */
    @Column(name = "collection_location")
    private String collectionLocation;

    /** Date the sample was actually collected in the field. */
    @Column(name = "collection_date")
    private LocalDate collectionDate;

    /** Additional notes or observations about the sample. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SampleStatus status = SampleStatus.RECEIVED;

    @Column(name = "received_at")
    private OffsetDateTime receivedAt = OffsetDateTime.now(ZoneOffset.UTC);

    /** Reason for rejection — populated when status = REJECTED. */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    /** Service order this sample belongs to (optional). */
    @ManyToOne
    @JoinColumn(name = "service_order_id")
    private ServiceOrder serviceOrder;

    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustodyEvent> custodyEvents = new ArrayList<>();

    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReanalysisRequest> reanalysisRequests = new ArrayList<>();
}