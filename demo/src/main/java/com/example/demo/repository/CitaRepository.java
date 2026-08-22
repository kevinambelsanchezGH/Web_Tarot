package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Cita;

// Repositorio de citas. Al extender JpaRepository ya obtenemos gratis
// findAll(), findById(), save(), deleteById(), etc. sin escribir código.
@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

}
