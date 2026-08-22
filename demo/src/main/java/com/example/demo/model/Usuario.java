package com.example.demo.model;

import jakarta.persistence.*;
import java.util.List;

// Entidad que representa un usuario/cliente registrado, con sus datos de contacto
// y la lista de citas que ha hecho. Se guarda en la tabla "usuarios".
@Entity
@Table(name = "usuarios")
public class Usuario {

    // Identificador único autogenerado por la base de datos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String nombre;
    private String apellidos;
    private String instagram;
    private String telefono;
    private String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getInstagram() {
        return instagram;
    }

    public void setInstagram(String instagram) {
        this.instagram = instagram;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public List<Cita> getCitas() {
        return citas;
    }

    public void setCitas(List<Cita> citas) {
        this.citas = citas;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // Todas las citas hechas por este usuario. Si se borra el usuario, se borran también sus citas (cascade ALL).
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL)
    private List<Cita> citas;


}
