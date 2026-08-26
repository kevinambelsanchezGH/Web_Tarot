package com.example.demo.service;

import com.example.demo.model.Cita;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

// Envía el email de confirmación de cita al cliente cuando su pago se completa.
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("es", "ES"));
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    // Un fallo de envío no debe romper la confirmación de la cita, que ya quedó guardada en BD:
    // solo se registra el error en el log.
    public void enviarConfirmacionCita(Cita cita) {
        try {
            Context context = new Context();
            context.setVariable("nombre", cita.getNombre());
            context.setVariable("fecha", cita.getFechaCita().format(FORMATO_FECHA));
            context.setVariable("hora", cita.getFechaCita().format(FORMATO_HORA));
            String html = templateEngine.process("email/confirmacion_cita", context);

            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, "UTF-8");
            helper.setTo(cita.getEmail());
            helper.setSubject("Tu cita de tarot está confirmada");
            helper.setText(html, true);
            mailSender.send(mensaje);

            log.info("Email de confirmación enviado a {} para la cita {}", cita.getEmail(), cita.getId());
        } catch (MessagingException | RuntimeException e) {
            log.error("No se pudo enviar el email de confirmación para la cita {}: {}", cita.getId(), e.getMessage(), e);
        }
    }
}
