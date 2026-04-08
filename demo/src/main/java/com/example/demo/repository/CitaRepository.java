package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.model.Cita;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {
    // Al extender JpaRepository, Spring ya sabe cómo hacer el 
    // SELECT, INSERT y DELETE en tu tabla 'citas'.
}