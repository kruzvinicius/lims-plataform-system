package com.kruzvinicius.limsbackend.repository;

import com.kruzvinicius.limsbackend.model.ServiceOrder;
import com.kruzvinicius.limsbackend.model.enums.ServiceOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ServiceOrderRepository extends JpaRepository<ServiceOrder, Long> {

    Optional<ServiceOrder> findByOrderNumber(String orderNumber);

    List<ServiceOrder> findByStatus(ServiceOrderStatus status);

    List<ServiceOrder> findByAssignedToUsername(String username);

    @Query("SELECT so FROM ServiceOrder so WHERE so.status NOT IN ('COMPLETED','CANCELLED') ORDER BY so.priority DESC, so.createdAt ASC")
    List<ServiceOrder> findPending();

    @Query("SELECT so FROM ServiceOrder so WHERE so.dueDate IS NOT NULL AND so.dueDate < :today AND so.status NOT IN ('COMPLETED','CANCELLED')")
    List<ServiceOrder> findOverdue(@Param("today") LocalDate today);

    @Query("SELECT COALESCE(MAX(so.id), 0) FROM ServiceOrder so")
    Long findMaxId();
}
