package com.example.demo.model;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// Entidad que representa un hueco horario en el que se puede reservar una cita.
// Se guarda en la tabla "disponibilidad".
@Entity
@Table(name = "disponibilidad")
public class Disponibilidad {

    // Identificador único autogenerado por la base de datos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Fecha y hora del hueco
    private LocalDateTime fechaHora;
    // true si ya hay una cita ocupando este hueco
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
