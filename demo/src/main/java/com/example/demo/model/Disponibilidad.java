package com.example.demo.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
@Table(name = "disponibilidad")
public class Disponibilidad { 
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private LocalDateTime fechaHora;
    private boolean reservada;

    // CONSTRUCTOR VACÍO
    public Disponibilidad() {
    }

    // CONSTRUCTOR CON DATOS
    public Disponibilidad(LocalDateTime fechaHora, boolean reservada) {
        this.fechaHora = fechaHora;
        this.reservada = reservada;
    }

    // GETTERS Y SETTERS
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    
    public boolean isReservada() { return reservada; }
    public void setReservada(boolean reservada) { this.reservada = reservada; }
}
