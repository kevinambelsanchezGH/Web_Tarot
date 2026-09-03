package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Cita;
import java.time.LocalDateTime;
import java.util.List;

// Repositorio de citas. Al extender JpaRepository ya obtenemos gratis
// findAll(), findById(), save(), deleteById(), etc. sin escribir código.
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Citas que llevan pendientes de pago desde antes de "limite" (para expirarlas).
    // Las que tienen fechaCreacion null (reservas de antes de esta funcionalidad) no entran aquí.
    List<Cita> findByEstadoAndFechaCreacionBefore(String estado, LocalDateTime limite);
}
