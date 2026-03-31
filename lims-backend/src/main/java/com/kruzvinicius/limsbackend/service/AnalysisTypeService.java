package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.AnalysisTypeDTO;
import com.kruzvinicius.limsbackend.dto.exception.EntityNotFoundException;
import com.kruzvinicius.limsbackend.model.AnalysisType;
import com.kruzvinicius.limsbackend.repository.AnalysisTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing analysis type definitions with acceptance ranges.
 */
@Service
@RequiredArgsConstructor
public class AnalysisTypeService {

    private final AnalysisTypeRepository repository;

    @Transactional
    public AnalysisTypeDTO create(AnalysisTypeDTO dto) {
        AnalysisType type = AnalysisType.builder()
                .code(dto.code().toUpperCase())
                .name(dto.name())
                .description(dto.description())
                .defaultUnit(dto.defaultUnit())
                .minValue(dto.minValue())
                .maxValue(dto.maxValue())
                .active(dto.active() != null ? dto.active() : true)
                .build();
        return mapToDTO(repository.save(type));
    }

    @Transactional
    public AnalysisTypeDTO update(Long id, AnalysisTypeDTO dto) {
        AnalysisType type = loadById(id);
        type.setCode(dto.code().toUpperCase());
        type.setName(dto.name());
        type.setDescription(dto.description());
        type.setDefaultUnit(dto.defaultUnit());
        type.setMinValue(dto.minValue());
        type.setMaxValue(dto.maxValue());
        if (dto.active() != null) type.setActive(dto.active());
        return mapToDTO(repository.save(type));
    }

    @Transactional(readOnly = true)
    public AnalysisTypeDTO findById(Long id) {
        return mapToDTO(loadById(id));
    }

    @Transactional(readOnly = true)
    public List<AnalysisTypeDTO> findAll() {
        return repository.findAll().stream().map(this::mapToDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<AnalysisTypeDTO> findActive() {
        return repository.findByActiveTrue().stream().map(this::mapToDTO).toList();
    }

    private AnalysisType loadById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Analysis Type not found: " + id));
    }

    private AnalysisTypeDTO mapToDTO(AnalysisType t) {
        return new AnalysisTypeDTO(
                t.getId(), t.getCode(), t.getName(), t.getDescription(),
                t.getDefaultUnit(), t.getMinValue(), t.getMaxValue(), t.isActive()
        );
    }
}
