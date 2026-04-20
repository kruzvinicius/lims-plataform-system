package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.LegislationDTO;
import com.kruzvinicius.limsbackend.dto.LegislationParameterDTO;
import com.kruzvinicius.limsbackend.dto.exception.EntityNotFoundException;
import com.kruzvinicius.limsbackend.model.AnalysisType;
import com.kruzvinicius.limsbackend.model.EnvironmentalLegislation;
import com.kruzvinicius.limsbackend.model.LegislationParameter;
import com.kruzvinicius.limsbackend.repository.AnalysisTypeRepository;
import com.kruzvinicius.limsbackend.repository.LegislationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegislationService {

    private final LegislationRepository legislationRepository;
    private final AnalysisTypeRepository analysisTypeRepository;

    @Transactional(readOnly = true)
    public List<LegislationDTO> findAll() {
        return legislationRepository.findAllByOrderByCodeAsc().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LegislationDTO> findActive() {
        return legislationRepository.findByActiveTrueOrderByCodeAsc().stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public LegislationDTO findById(Long id) {
        return mapToDTO(loadById(id));
    }

    @Transactional
    public LegislationDTO create(LegislationDTO dto) {
        EnvironmentalLegislation leg = EnvironmentalLegislation.builder()
                .code(dto.code().toUpperCase())
                .name(dto.name())
                .region(dto.region())
                .description(dto.description())
                .active(dto.active() != null ? dto.active() : true)
                .build();

        leg = legislationRepository.save(leg);

        if (dto.parameters() != null) {
            for (LegislationParameterDTO pDto : dto.parameters()) {
                addParameter(leg, pDto);
            }
        }

        // Save again so CascadeType.ALL persists the child parameters
        leg = legislationRepository.save(leg);

        return mapToDTO(leg);
    }

    @Transactional
    public LegislationDTO update(Long id, LegislationDTO dto) {
        EnvironmentalLegislation leg = loadById(id);
        leg.setCode(dto.code().toUpperCase());
        leg.setName(dto.name());
        leg.setRegion(dto.region());
        leg.setDescription(dto.description());
        if (dto.active() != null) leg.setActive(dto.active());

        // Replace all parameters
        leg.getParameters().clear();
        if (dto.parameters() != null) {
            for (LegislationParameterDTO pDto : dto.parameters()) {
                addParameter(leg, pDto);
            }
        }

        return mapToDTO(legislationRepository.save(leg));
    }

    @Transactional
    public void delete(Long id) {
        EnvironmentalLegislation leg = loadById(id);
        legislationRepository.delete(leg);
        log.info("Deleted legislation {}", id);
    }

    // --- private helpers ---

    private void addParameter(EnvironmentalLegislation leg, LegislationParameterDTO pDto) {
        AnalysisType at = analysisTypeRepository.findById(pDto.analysisTypeId())
                .orElseThrow(() -> new EntityNotFoundException("AnalysisType not found: " + pDto.analysisTypeId()));
        LegislationParameter param = LegislationParameter.builder()
                .legislation(leg)
                .analysisType(at)
                .vmpMin(pDto.vmpMin())
                .vmpMax(pDto.vmpMax())
                .unit(pDto.unit() != null ? pDto.unit() : at.getDefaultUnit())
                .notes(pDto.notes())
                .build();
        leg.getParameters().add(param);
    }

    private EnvironmentalLegislation loadById(Long id) {
        return legislationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Legislation not found: " + id));
    }

    private LegislationDTO mapToDTO(EnvironmentalLegislation leg) {
        List<LegislationParameterDTO> params = leg.getParameters() == null
                ? Collections.emptyList()
                : leg.getParameters().stream().map(p -> new LegislationParameterDTO(
                        p.getId(),
                        p.getAnalysisType().getId(),
                        p.getAnalysisType().getCode(),
                        p.getAnalysisType().getName(),
                        p.getAnalysisType().getDefaultUnit(),
                        p.getVmpMin(),
                        p.getVmpMax(),
                        p.getUnit(),
                        p.getNotes()
                )).toList();

        return new LegislationDTO(
                leg.getId(),
                leg.getCode(),
                leg.getName(),
                leg.getRegion(),
                leg.getDescription(),
                leg.isActive(),
                params
        );
    }
}

