package com.kruzvinicius.limsbackend.scheduling;

import com.kruzvinicius.limsbackend.dto.EquipmentDTO;
import com.kruzvinicius.limsbackend.dto.ServiceOrderDTO;
import com.kruzvinicius.limsbackend.service.EquipmentService;
import com.kruzvinicius.limsbackend.service.NotificationService;
import com.kruzvinicius.limsbackend.service.ServiceOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled tasks that run daily to detect and alert on:
 *  1. Equipment calibrations due within 7 days
 *  2. Equipment maintenance due within 7 days
 *  3. Overdue Service Orders (SLA breached)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class LimsScheduler {

    private final EquipmentService equipmentService;
    private final ServiceOrderService serviceOrderService;
    private final NotificationService notificationService;

    /** Runs every day at 07:00 AM server time. */
    @Scheduled(cron = "0 0 7 * * *")
    public void dailyAlerts() {
        log.info("=== LIMS Daily Alert Check started ===");
        checkCalibrationsDue();
        checkMaintenanceDue();
        checkOverdueServiceOrders();
        log.info("=== LIMS Daily Alert Check completed ===");
    }

    private void checkCalibrationsDue() {
        List<EquipmentDTO> dueEquipment = equipmentService.findDueForCalibration(7);
        if (dueEquipment.isEmpty()) {
            log.info("No equipment due for calibration in the next 7 days.");
            return;
        }

        log.warn("{} equipment(s) due for calibration", dueEquipment.size());
        for (EquipmentDTO eq : dueEquipment) {
            notificationService.sendCalibrationAlert(
                    "manager@lims.local",
                    eq.name() + " (S/N: " + eq.serialNumber() + ")",
                    eq.nextCalibrationDue() != null ? eq.nextCalibrationDue().toString() : "N/A"
            );
        }
    }

    private void checkMaintenanceDue() {
        List<EquipmentDTO> dueEquipment = equipmentService.findDueForMaintenance(7);
        if (dueEquipment.isEmpty()) {
            log.info("No equipment due for maintenance in the next 7 days.");
            return;
        }

        log.warn("{} equipment(s) due for maintenance", dueEquipment.size());
        for (EquipmentDTO eq : dueEquipment) {
            notificationService.sendEmail(
                    "manager@lims.local",
                    "Manutenção Prevista — " + eq.name(),
                    "O equipamento '" + eq.name() + "' (S/N: " + eq.serialNumber() + ") " +
                    "tem manutenção prevista para " + eq.nextMaintenanceDue() + ".\n" +
                    "— LIMS System"
            );
        }
    }

    private void checkOverdueServiceOrders() {
        List<ServiceOrderDTO> overdueOrders = serviceOrderService.findOverdue();
        if (overdueOrders.isEmpty()) {
            log.info("No overdue service orders found.");
            return;
        }

        log.warn("{} service order(s) are OVERDUE", overdueOrders.size());
        for (ServiceOrderDTO so : overdueOrders) {
            notificationService.sendSlaAlert(
                    "manager@lims.local",
                    so.orderNumber(),
                    so.dueDate() != null ? so.dueDate().toString() : "N/A"
            );
        }
    }
}
