package com.example.demo.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "pagos")

public class Pago {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Double importe;
    private String metodoPago;//Bizum, Tarjeta, Paypal
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

    public Pago() {}

    
}
