package com.example.demo.service;

import com.example.demo.model.Cita;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Crea, en el calendario de Google, un evento por cada cita pagada.
@Service
public class GoogleCalendarService {

    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarService.class);
    private static final ZoneId ZONA = ZoneId.of("Europe/Madrid");

    @Value("${google.calendar.credentials-path}")
    private String credencialesPath;

    @Value("${google.calendar.calendar-id}")
    private String calendarId;

    @Value("${google.calendar.event-duration-minutes}")
    private int duracionMinutos;

    private Calendar calendarClient;

    // Se ejecuta una sola vez al arrancar la app: si faltan credenciales o calendar-id
    // (todavía no configurados), lo dejamos en null y crearEventoCita() lo detecta y no falla.
    @PostConstruct
    private void init() {
        if (credencialesPath == null || credencialesPath.isBlank()
                || calendarId == null || calendarId.isBlank()) {
            log.warn("Google Calendar no está configurado (falta credentials-path o calendar-id); no se crearán eventos.");
            return;
        }
        try (FileInputStream credencialesStream = new FileInputStream(credencialesPath)) {
            GoogleCredentials credenciales = GoogleCredentials.fromStream(credencialesStream)
                    .createScoped(List.of(CalendarScopes.CALENDAR_EVENTS));

            calendarClient = new Calendar.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credenciales))
                    .setApplicationName("Dualtarot")
                    .build();
        } catch (Exception e) {
            log.error("No se pudo inicializar el cliente de Google Calendar: {}", e.getMessage(), e);
        }
    }

    // Un fallo aquí no debe romper la confirmación de la cita, que ya quedó guardada en BD:
    // solo se registra el error en el log.
    public void crearEventoCita(Cita cita) {
        if (calendarClient == null) {
            log.warn("Google Calendar no inicializado; se omite la creación del evento para la cita {}", cita.getId());
            return;
        }
        try {
            ZonedDateTime inicio = cita.getFechaCita().atZone(ZONA);
            ZonedDateTime fin = inicio.plusMinutes(duracionMinutos);

            Event evento = new Event()
                    .setSummary("Cita Tarot - " + cita.getNombre() + " " + cita.getApellidos())
                    .setDescription("Tel: " + cita.getTelefono()
                            + " | Email: " + cita.getEmail()
                            + " | Instagram: " + cita.getInstagram())
                    .setStart(new EventDateTime().setDateTime(aFechaGoogle(inicio)))
                    .setEnd(new EventDateTime().setDateTime(aFechaGoogle(fin)));

            calendarClient.events().insert(calendarId, evento).execute();
            log.info("Evento de Google Calendar creado para la cita {}", cita.getId());
        } catch (Exception e) {
            log.error("No se pudo crear el evento de Google Calendar para la cita {}: {}", cita.getId(), e.getMessage(), e);
        }
    }

    private DateTime aFechaGoogle(ZonedDateTime zonedDateTime) {
        return new DateTime(Date.from(zonedDateTime.toInstant()), java.util.TimeZone.getTimeZone(ZONA));
    }
}
