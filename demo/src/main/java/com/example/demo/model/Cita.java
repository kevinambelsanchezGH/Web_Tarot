package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="citas")

public class Cita {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "disponibilidad_id") 
    private Disponibilidad disponibilidad;

    private LocalDateTime fechaCita;
    private String estado;
    private String nombre;
    private String apellidos;
    private String instagram;
    private String telefono;
    private String email;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Usuario getUsuario() {return usuario;}
    public void setUsuario(Usuario usuario) {this.usuario = usuario;}

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}

    public LocalDateTime getFechaCita() {return fechaCita;}
    public void setFechaCita(LocalDateTime fechaCita) {this.fechaCita = fechaCita;}

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
