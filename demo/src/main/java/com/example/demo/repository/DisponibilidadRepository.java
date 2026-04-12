package com.example.demo.repository;

import com.example.demo.model.Disponibilidad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DisponibilidadRepository extends JpaRepository<Disponibilidad, Long> {
    List<Disponibilidad> findByReservadaFalse();
}