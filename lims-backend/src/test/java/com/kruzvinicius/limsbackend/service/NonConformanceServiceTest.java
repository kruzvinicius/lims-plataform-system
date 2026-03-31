package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.NonConformanceDTO;
import com.kruzvinicius.limsbackend.dto.exception.EntityNotFoundException;
import com.kruzvinicius.limsbackend.model.*;
import com.kruzvinicius.limsbackend.model.enums.*;
import com.kruzvinicius.limsbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NonConformanceServiceTest {

    @Mock private NonConformanceRepository ncRepository;
    @Mock private UserRepository userRepository;
    @Mock private SampleRepository sampleRepository;
    @Mock private TestResultRepository testResultRepository;
    @Mock private NotificationService notificationService;

    @InjectMocks private NonConformanceService ncService;

    private User detector;
    private User investigator;

    @BeforeEach
    void setUp() {
        detector = User.builder().id(1L).username("analyst1").role(Role.ANALYST).password("pass").build();
        investigator = User.builder().id(2L).username("manager1").role(Role.MANAGER).password("pass").build();
    }

    private NonConformance buildNC(NonConformanceStatus status) {
        return NonConformance.builder()
                .id(1L)
                .title("pH fora da faixa")
                .description("Resultado 2.5, esperado 6.5-8.5")
                .type(NonConformanceType.RESULT_OUT_OF_RANGE)
                .severity(NonConformanceSeverity.HIGH)
                .status(status)
                .detectedBy(detector)
                .build();
    }

    @Nested
    @DisplayName("Lifecycle")
    class Lifecycle {

        @Test
        @DisplayName("should create NC with OPEN status")
        void shouldCreate() {
            when(userRepository.findByUsername("analyst1")).thenReturn(Optional.of(detector));
            when(ncRepository.save(any())).thenAnswer(inv -> {
                NonConformance nc = inv.getArgument(0);
                nc.setId(1L);
                return nc;
            });

            NonConformanceDTO dto = new NonConformanceDTO(null, "pH fora", "desc",
                    NonConformanceType.RESULT_OUT_OF_RANGE, NonConformanceSeverity.HIGH,
                    null, null, null, null, null, null,
                    "analyst1", null, null, null);

            NonConformanceDTO result = ncService.create(dto);
            assertThat(result.status()).isEqualTo(NonConformanceStatus.OPEN);
        }

        @Test
        @DisplayName("OPEN → UNDER_INVESTIGATION via assign")
        void shouldAssign() {
            NonConformance nc = buildNC(NonConformanceStatus.OPEN);
            when(ncRepository.findById(1L)).thenReturn(Optional.of(nc));
            when(userRepository.findByUsername("manager1")).thenReturn(Optional.of(investigator));
            when(ncRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            NonConformanceDTO result = ncService.assign(1L, "manager1");
            assertThat(result.status()).isEqualTo(NonConformanceStatus.UNDER_INVESTIGATION);
        }

        @Test
        @DisplayName("UNDER_INVESTIGATION → RESOLVED")
        void shouldResolve() {
            NonConformance nc = buildNC(NonConformanceStatus.UNDER_INVESTIGATION);
            when(ncRepository.findById(1L)).thenReturn(Optional.of(nc));
            when(ncRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            NonConformanceDTO result = ncService.resolve(1L, "Reagente vencido", "Trocar fornecedor");
            assertThat(result.status()).isEqualTo(NonConformanceStatus.RESOLVED);
            assertThat(result.correctiveAction()).isEqualTo("Reagente vencido");
            assertThat(result.resolvedAt()).isNotNull();
        }

        @Test
        @DisplayName("RESOLVED → CLOSED")
        void shouldClose() {
            NonConformance nc = buildNC(NonConformanceStatus.RESOLVED);
            when(ncRepository.findById(1L)).thenReturn(Optional.of(nc));
            when(ncRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            NonConformanceDTO result = ncService.close(1L);
            assertThat(result.status()).isEqualTo(NonConformanceStatus.CLOSED);
        }

        @Test
        @DisplayName("should reject resolve on OPEN NC")
        void shouldRejectResolveOnOpen() {
            NonConformance nc = buildNC(NonConformanceStatus.OPEN);
            when(ncRepository.findById(1L)).thenReturn(Optional.of(nc));

            assertThatThrownBy(() -> ncService.resolve(1L, "cause", "action"))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("should reject close on OPEN NC")
        void shouldRejectCloseOnOpen() {
            NonConformance nc = buildNC(NonConformanceStatus.OPEN);
            when(ncRepository.findById(1L)).thenReturn(Optional.of(nc));

            assertThatThrownBy(() -> ncService.close(1L))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    @DisplayName("should throw when NC not found")
    void shouldThrowWhenNotFound() {
        when(ncRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> ncService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
