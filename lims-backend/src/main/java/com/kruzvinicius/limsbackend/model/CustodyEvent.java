package com.kruzvinicius.limsbackend.model;

import com.kruzvinicius.limsbackend.model.enums.CustodyEventType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.Audited;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Records a single event in the chain of custody of a laboratory sample.
 * Every time the physical possession or location of a sample changes,
 * a new CustodyEvent is created for traceability.
 */
@Entity
@Audited
@Table(name = "custody_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustodyEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Type of custody change (RECEIVED, TRANSFERRED, RETURNED, DISPOSED). */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private CustodyEventType eventType;

    /** Physical location or section where the event took place. */
    @Column(nullable = false)
    private String location;

    /** Free-text notes about the custody event. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /** Timestamp when the custody event occurred. */
    @Builder.Default
    @Column(name = "occurred_at", nullable = false)
    private OffsetDateTime occurredAt = OffsetDateTime.now(ZoneOffset.UTC);

    /** The user who performed or registered the custody transfer. */
    @ManyToOne
    @JoinColumn(name = "transferred_by", nullable = false)
    private User transferredBy;

    /** The sample to which this event belongs. */
    @ManyToOne
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;
}
