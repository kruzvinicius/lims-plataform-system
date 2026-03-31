package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.AnalysisTypeDTO;
import com.kruzvinicius.limsbackend.dto.exception.EntityNotFoundException;
import com.kruzvinicius.limsbackend.model.AnalysisType;
import com.kruzvinicius.limsbackend.repository.AnalysisTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalysisTypeServiceTest {

    @Mock private AnalysisTypeRepository repository;
    @InjectMocks private AnalysisTypeService service;

    @Test
    @DisplayName("should create analysis type with uppercase code")
    void shouldCreate() {
        when(repository.save(any())).thenAnswer(inv -> {
            AnalysisType t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        AnalysisTypeDTO dto = new AnalysisTypeDTO(null, "ph", "pH", "pH measure",
                "pH units", new BigDecimal("6.5"), new BigDecimal("8.5"), true);

        AnalysisTypeDTO result = service.create(dto);
        assertThat(result.code()).isEqualTo("PH");
        assertThat(result.minValue()).isEqualByComparingTo("6.5");
        assertThat(result.maxValue()).isEqualByComparingTo("8.5");
    }

    @Test
    @DisplayName("should update analysis type")
    void shouldUpdate() {
        AnalysisType existing = AnalysisType.builder()
                .id(1L).code("PH").name("pH").active(true).build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AnalysisTypeDTO dto = new AnalysisTypeDTO(null, "PH", "pH Atualizado", null,
                null, new BigDecimal("6.0"), new BigDecimal("9.0"), true);

        AnalysisTypeDTO result = service.update(1L, dto);
        assertThat(result.name()).isEqualTo("pH Atualizado");
    }

    @Test
    @DisplayName("should find active types only")
    void shouldFindActive() {
        AnalysisType active = AnalysisType.builder().id(1L).code("PH").name("pH").active(true).build();
        when(repository.findByActiveTrue()).thenReturn(List.of(active));

        List<AnalysisTypeDTO> result = service.findActive();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).active()).isTrue();
    }

    @Test
    @DisplayName("should throw when not found")
    void shouldThrowNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
