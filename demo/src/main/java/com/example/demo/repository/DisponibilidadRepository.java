package com.example.demo.repository;

import com.example.demo.model.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

// Repositorio de huecos de disponibilidad.
public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {
    // Devuelve solo los huecos que todavía están libres (reservada = false),
    // Spring Data genera la consulta automáticamente a partir del nombre del método.
    List<Disponibilidad> findByReservadaFalse();

    // Busca un hueco por id bloqueando la fila en la base de datos (bloqueo pesimista de escritura)
    // hasta que termine la transacción. Así se evita que dos clientes reserven el mismo hueco a la vez.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Disponibilidad d WHERE d.id = :id")
    Optional<Disponibilidad> findByIdWithLock(@Param("id") Long id);
}
