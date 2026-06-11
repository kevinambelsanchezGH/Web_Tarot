package com.example.demo.repository;

import com.example.demo.model.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {
    List<Disponibilidad> findByReservadaFalse();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Disponibilidad d WHERE d.id = :id")
    Optional<Disponibilidad> findByIdWithLock(@Param("id") Long id);
}