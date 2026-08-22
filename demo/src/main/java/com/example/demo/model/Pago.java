package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// Entidad que representa el pago realizado (vía Stripe) para una cita.
// Se guarda en la tabla "pagos".
@Entity
@Table(name = "pagos")

public class Pago {
    // Identificador único autogenerado por la base de datos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // Cantidad cobrada
    private Double importe;
    // Cómo se pagó (ej. "Stripe Card")
    private String metodoPago;
    // Cuándo se registró el pago
    private LocalDateTime fechaPago;
    private String estado;//PENDIENTE, COMPLETADO

//Relacion 1 a 1, un pago pertenece a una unica cita
    @OneToOne
    @JoinColumn(name = "cita_id")
    private Cita cita;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getImporte() {
        return importe;
    }

    public void setImporte(Double importe) {
        this.importe = importe;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public LocalDateTime getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Cita getCita() {
        return cita;
    }

    public void setCita(Cita cita) {
        this.cita = cita;
    }

    // Constructor vacío requerido por JPA
    public Pago() {}


}
