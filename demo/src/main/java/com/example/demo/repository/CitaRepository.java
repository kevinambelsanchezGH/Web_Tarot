package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Quita la referencia al hueco en las citas que apuntan a él (p. ej. citas EXPIRADAS),
    // para poder borrar el hueco sin violar la clave foránea. La cita conserva su fechaCita.
    @Modifying
    @Query("UPDATE Cita c SET c.disponibilidad = null WHERE c.disponibilidad.id = :huecoId")
    int desvincularHueco(@Param("huecoId") Long huecoId);
}
