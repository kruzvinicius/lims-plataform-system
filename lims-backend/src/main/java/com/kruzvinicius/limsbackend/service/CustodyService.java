package com.kruzvinicius.limsbackend.service;


import com.kruzvinicius.limsbackend.dto.CustodyEventDTO;
import com.kruzvinicius.limsbackend.dto.exception.EntityNotFoundException;
import com.kruzvinicius.limsbackend.model.CustodyEvent;
import com.kruzvinicius.limsbackend.model.Sample;
import com.kruzvinicius.limsbackend.model.User;
import com.kruzvinicius.limsbackend.repository.CustodyEventRepository;
import com.kruzvinicius.limsbackend.repository.SampleRepository;
import com.kruzvinicius.limsbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Service for managing the chain of custody of laboratory samples.
 * Every physical transfer or key event involving a sample is recorded here.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustodyService {

    private final CustodyEventRepository custodyEventRepository;
    private final SampleRepository sampleRepository;
    private final UserRepository userRepository;

    /**
     * Register a new custody event for a sample.
     *
     * @param sampleId the target sample
     * @param dto      event details (type, location, notes)
     * @param username the user performing the custody transfer
     * @return the persisted custody event as a DTO
     */
    @Transactional
    public CustodyEventDTO registerEvent(Long sampleId, CustodyEventDTO dto, String username) {
        Sample sample = sampleRepository.findById(sampleId)
                .orElseThrow(() -> new EntityNotFoundException("Sample not found: " + sampleId));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));

        CustodyEvent event = CustodyEvent.builder()
                .sample(sample)
                .eventType(dto.eventType())
                .location(dto.location())
                .notes(dto.notes())
                .occurredAt(OffsetDateTime.now(ZoneOffset.UTC))
                .transferredBy(user)
                .build();

        CustodyEvent saved = custodyEventRepository.save(event);
        log.info("Custody event {} registered for sample {} by {}", saved.getEventType(), sampleId, username);
        return mapToDTO(saved);
    }

    /**
     * Retrieve the full chain of custody for a sample, ordered chronologically.
     */
    @Transactional(readOnly = true)
    public List<CustodyEventDTO> getChain(Long sampleId) {
        if (!sampleRepository.existsById(sampleId))
            throw new EntityNotFoundException("Sample not found: " + sampleId);

        return custodyEventRepository.findBySampleIdOrderByOccurredAtAsc(sampleId)
                .stream().map(this::mapToDTO).toList();
    }

    // ── MAPPER ────────────────────────────────────────────────────────────────

    private CustodyEventDTO mapToDTO(CustodyEvent event) {
        return new CustodyEventDTO(
                event.getId(),
                event.getEventType(),
                event.getLocation(),
                event.getNotes(),
                event.getOccurredAt(),
                event.getTransferredBy() != null ? event.getTransferredBy().getUsername() : null
        );
    }
}
