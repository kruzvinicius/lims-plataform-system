package com.kruzvinicius.limsbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service for sending email notifications.
 * Disabled by default — set spring.mail.host to enable.
 * Used for alerts: calibration due, SLA overdue, NC opened, etc.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@lims.local}")
    private String fromAddress;

    @Value("${lims.notifications.enabled:false}")
    private boolean notificationsEnabled;

    /**
     * Send a simple email notification.
     * No-op if notifications are disabled.
     */
    public void sendEmail(String to, String subject, String body) {
        if (!notificationsEnabled) {
            log.debug("Notifications disabled — skipping email to {} : {}", to, subject);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(to);
            message.setSubject("[LIMS] " + subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to {} : {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {} : {}", to, subject, e);
        }
    }

    /**
     * Alert: equipment calibration is due or overdue.
     */
    public void sendCalibrationAlert(String to, String equipmentName, String dueDate) {
        sendEmail(to,
                "Calibração Vencida — " + equipmentName,
                "O equipamento '" + equipmentName + "' tem calibração prevista para " + dueDate + ".\n" +
                "Por favor, providencie a calibração o mais breve possível.\n\n" +
                "— LIMS System"
        );
    }

    /**
     * Alert: service order SLA is overdue.
     */
    public void sendSlaAlert(String to, String orderNumber, String dueDate) {
        sendEmail(to,
                "SLA Estourado — " + orderNumber,
                "A Ordem de Serviço " + orderNumber + " tinha prazo para " + dueDate + " e ainda não foi concluída.\n" +
                "Ação imediata é necessária.\n\n" +
                "— LIMS System"
        );
    }

    /**
     * Alert: new non-conformance opened.
     */
    public void sendNonConformanceAlert(String to, String ncTitle, String severity) {
        sendEmail(to,
                "Nova Não Conformidade — " + severity,
                "Uma nova não conformidade foi registrada:\n\n" +
                "Título: " + ncTitle + "\n" +
                "Severidade: " + severity + "\n\n" +
                "Acesse o sistema para mais detalhes.\n\n" +
                "— LIMS System"
        );
    }
}
