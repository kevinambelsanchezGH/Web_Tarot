package com.example.demo.service;

import com.example.demo.model.Cita;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.DisponibilidadRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Cada minuto revisa si hay citas PENDIENTE cuyo plazo de pago ya ha vencido, y si es así,
// libera el hueco horario para que otro cliente pueda reservarlo.
@Service
public class LimpiezaReservasService {

    private static final Logger log = LoggerFactory.getLogger(LimpiezaReservasService.class);

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    @Value("${reserva.expiracion-minutos}")
    private long expiracionMinutos;

    // Referencia a sí mismo a través del proxy de Spring: si expirarUnaReserva() se llamara
    // directamente como "this.expirarUnaReserva(...)", Spring no aplicaría su @Transactional
    // (las llamadas internas a métodos de la misma clase no pasan por el proxy). Llamando a
    // través de "self" sí pasa por el proxy, y el bloqueo pesimista funciona correctamente.
    @Autowired
    @Lazy
    private LimpiezaReservasService self;

    @Scheduled(fixedRate = 60000) // cada 60 segundos
    public void expirarReservasSinPagar() {
        LocalDateTime limite = LocalDateTime.now().minusMinutes(expiracionMinutos);

        // Solo entran aquí citas PENDIENTE con fechaCreacion rellena y anterior al límite.
        // Las reservas de antes de esta funcionalidad (fechaCreacion = null) no se tocan.
        List<Cita> citasVencidas = citaRepository.findByEstadoAndFechaCreacionBefore("PENDIENTE", limite);

        for (Cita cita : citasVencidas) {
            self.expirarUnaReserva(cita.getId());
        }
    }

    // Cada cita se procesa en su propia transacción corta, para no dejar bloqueada
    // la tabla más tiempo del necesario y para que un fallo en una no afecte a las demás.
    @Transactional
    public void expirarUnaReserva(Long citaId) {
        Optional<Cita> citaOpt = citaRepository.findById(citaId);
        if (citaOpt.isEmpty()) {
            return;
        }
        Cita cita = citaOpt.get();

        // Reconfirmamos que sigue PENDIENTE: puede que justo se acabase de pagar
        // (en pagoExito()) entre la consulta de arriba y este momento.
        if (!"PENDIENTE".equals(cita.getEstado())) {
            return;
        }

        if (cita.getDisponibilidad() != null) {
            // Bloqueo pesimista, el mismo que usa CitaController al crear una reserva,
            // para no chocar con otra operación sobre el mismo hueco.
            disponibilidadRepository.findByIdWithLock(cita.getDisponibilidad().getId())
                    .ifPresent(hueco -> {
                        hueco.setReservada(false);
                        disponibilidadRepository.save(hueco);
                    });
        }

        cita.setEstado("EXPIRADA");
        citaRepository.save(cita);

        log.info("Cita {} expirada por falta de pago; hueco liberado", citaId);
    }
}
