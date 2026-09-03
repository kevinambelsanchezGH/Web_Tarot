package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Entidad que representa una cita/reserva de tarot hecha por un cliente.
// Se guarda en la tabla "citas" y enlaza con el hueco horario (Disponibilidad)
// y, opcionalmente, con el Usuario que la hizo.
@Entity
@Table(name="citas")

public class Cita {
    // Identificador único autogenerado por la base de datos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Hueco horario que ocupa esta cita
    @ManyToOne
    @JoinColumn(name = "disponibilidad_id")
    private Disponibilidad disponibilidad;

    // Fecha y hora reales de la cita (copiadas del hueco al crearla)
    private LocalDateTime fechaCita;
    // Momento en el que se creó la reserva (para poder expirarla si tarda en pagarse)
    private LocalDateTime fechaCreacion;
    // Estado de la cita: PENDIENTE, PAGADO, EXPIRADA, etc.
    private String estado;
    // Datos de contacto del cliente, rellenados desde el formulario de reserva
    private String nombre;
    private String apellidos;
    private String instagram;
    private String telefono;
    private String email;

    // Usuario registrado al que pertenece la cita (si el sistema de usuarios llega a usarse)
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Usuario getUsuario() {return usuario;}
    public void setUsuario(Usuario usuario) {this.usuario = usuario;}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public LocalDateTime getFechaCita() {return fechaCita;}
    public void setFechaCita(LocalDateTime fechaCita) {this.fechaCita = fechaCita;}

    public LocalDateTime getFechaCreacion() {return fechaCreacion;}
    public void setFechaCreacion(LocalDateTime fechaCreacion) {this.fechaCreacion = fechaCreacion;}

    public String getEstado() {return estado;}
    public void setEstado(String estado) {this.estado = estado;}

    public Disponibilidad getDisponibilidad() {return disponibilidad;}
    public void setDisponibilidad(Disponibilidad disponibilidad) {this.disponibilidad = disponibilidad;}

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getInstagram() { return instagram; }
    public void setInstagram(String instagram) { this.instagram = instagram; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
