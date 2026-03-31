package com.kruzvinicius.limsbackend.service;

import com.kruzvinicius.limsbackend.dto.ServiceOrderDTO;
import com.kruzvinicius.limsbackend.dto.exception.EntityNotFoundException;
import com.kruzvinicius.limsbackend.model.*;
import com.kruzvinicius.limsbackend.model.enums.ServiceOrderPriority;
import com.kruzvinicius.limsbackend.model.enums.ServiceOrderStatus;
import com.kruzvinicius.limsbackend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOrderServiceTest {

    @Mock private ServiceOrderRepository soRepository;
    @Mock private CustomerRepository customerRepository;
    @Mock private UserRepository userRepository;
    @Mock private SampleRepository sampleRepository;

    @InjectMocks private ServiceOrderService serviceOrderService;

    private Customer customer;
    private User creator;
    private User analyst;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setCorporateReason("Lab Central");
        creator = User.builder().id(1L).username("admin").role(Role.ADMIN).password("pass").build();
        analyst = User.builder().id(2L).username("analyst1").role(Role.ANALYST).password("pass").build();
    }

    private ServiceOrder buildOrder(ServiceOrderStatus status) {
        return ServiceOrder.builder()
                .id(1L)
                .orderNumber("OS-2026-0001")
                .description("Test order")
                .status(status)
                .priority(ServiceOrderPriority.NORMAL)
                .customer(customer)
                .createdBy(creator)
                .samples(new ArrayList<>())
                .build();
    }

    @Nested
    @DisplayName("Create")
    class Create {
        @Test
        @DisplayName("should create service order with generated number")
        void shouldCreate() {
            when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
            when(userRepository.findByUsername("admin")).thenReturn(Optional.of(creator));
            when(soRepository.findMaxId()).thenReturn(0L);
            when(soRepository.save(any(ServiceOrder.class))).thenAnswer(inv -> {
                ServiceOrder o = inv.getArgument(0);
                o.setId(1L);
                o.setSamples(new ArrayList<>());
                return o;
            });
            when(soRepository.findById(1L)).thenAnswer(inv -> {
                ServiceOrder o = buildOrder(ServiceOrderStatus.CREATED);
                return Optional.of(o);
            });

            ServiceOrderDTO dto = new ServiceOrderDTO(null, null, "Test", null,
                    ServiceOrderPriority.HIGH, null, null, null, null, null,
                    1L, null, null, null);

            ServiceOrderDTO result = serviceOrderService.create(dto, "admin");

            assertThat(result).isNotNull();
            assertThat(result.orderNumber()).startsWith("OS-");
            verify(soRepository).save(any());
        }

        @Test
        @DisplayName("should fail if customer not found")
        void shouldFailCustomerNotFound() {
            when(customerRepository.findById(999L)).thenReturn(Optional.empty());
            ServiceOrderDTO dto = new ServiceOrderDTO(null, null, "Test", null, null, null, null,
                    null, null, null, 999L, null, null, null);

            assertThatThrownBy(() -> serviceOrderService.create(dto, "admin"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("State machine")
    class StateMachine {
        @Test
        @DisplayName("CREATED → ASSIGNED")
        void shouldAssign() {
            ServiceOrder order = buildOrder(ServiceOrderStatus.CREATED);
            when(soRepository.findById(1L)).thenReturn(Optional.of(order));
            when(userRepository.findByUsername("analyst1")).thenReturn(Optional.of(analyst));
            when(soRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ServiceOrderDTO result = serviceOrderService.assign(1L, "analyst1");
            assertThat(result.status()).isEqualTo(ServiceOrderStatus.ASSIGNED);
            assertThat(result.assignedTo()).isEqualTo("analyst1");
        }

        @Test
        @DisplayName("ASSIGNED → IN_PROGRESS")
        void shouldStart() {
            ServiceOrder order = buildOrder(ServiceOrderStatus.ASSIGNED);
            when(soRepository.findById(1L)).thenReturn(Optional.of(order));
            when(soRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ServiceOrderDTO result = serviceOrderService.start(1L);
            assertThat(result.status()).isEqualTo(ServiceOrderStatus.IN_PROGRESS);
        }

        @Test
        @DisplayName("IN_PROGRESS → COMPLETED")
        void shouldComplete() {
            ServiceOrder order = buildOrder(ServiceOrderStatus.IN_PROGRESS);
            when(soRepository.findById(1L)).thenReturn(Optional.of(order));
            when(soRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ServiceOrderDTO result = serviceOrderService.complete(1L);
            assertThat(result.status()).isEqualTo(ServiceOrderStatus.COMPLETED);
            assertThat(result.completedAt()).isNotNull();
        }

        @Test
        @DisplayName("should reject invalid transition CREATED → COMPLETED")
        void shouldRejectInvalidTransition() {
            ServiceOrder order = buildOrder(ServiceOrderStatus.CREATED);
            when(soRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> serviceOrderService.complete(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot transition");
        }

        @Test
        @DisplayName("should cancel with reason")
        void shouldCancel() {
            ServiceOrder order = buildOrder(ServiceOrderStatus.IN_PROGRESS);
            when(soRepository.findById(1L)).thenReturn(Optional.of(order));
            when(soRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ServiceOrderDTO result = serviceOrderService.cancel(1L, "Customer withdrew");
            assertThat(result.status()).isEqualTo(ServiceOrderStatus.CANCELLED);
            assertThat(result.cancellationReason()).isEqualTo("Customer withdrew");
        }

        @Test
        @DisplayName("should reject cancelling already completed order")
        void shouldRejectCancelCompleted() {
            ServiceOrder order = buildOrder(ServiceOrderStatus.COMPLETED);
            when(soRepository.findById(1L)).thenReturn(Optional.of(order));

            assertThatThrownBy(() -> serviceOrderService.cancel(1L, "reason"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Queries")
    class Queries {
        @Test
        @DisplayName("should find overdue orders")
        void shouldFindOverdue() {
            ServiceOrder overdue = buildOrder(ServiceOrderStatus.IN_PROGRESS);
            overdue.setDueDate(LocalDate.now().minusDays(3));
            when(soRepository.findOverdue(any())).thenReturn(List.of(overdue));

            List<ServiceOrderDTO> result = serviceOrderService.findOverdue();
            assertThat(result).hasSize(1);
        }
    }
}
