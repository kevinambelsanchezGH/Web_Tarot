package com.example.demo.controller;

import com.example.demo.model.Cita;
import com.example.demo.model.Usuario;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller 
@RequestMapping("/citas")
public class CitaController {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Este método recibe los datos del formulario HTML
    @PostMapping("/guardar")
    public String guardarCita(@RequestParam String nombre,
                              @RequestParam String apellidos,
                              @RequestParam String instagram,
                              @RequestParam String telefono,
                              @RequestParam String email,
                              @RequestParam String fechaCita) {
        
        // 1. Creamos y guardamos el Usuario
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(nombre);
        nuevoUsuario.setApellidos(apellidos);
        nuevoUsuario.setInstagram(instagram);
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setTelefono(telefono);
        usuarioRepository.save(nuevoUsuario);

        // 2. Creamos y guardamos la Cita unida a ese usuario
        Cita nuevaCita = new Cita();
        nuevaCita.setFechaCita(LocalDateTime.parse(fechaCita));
        nuevaCita.setEstado("PENDIENTE");
        nuevaCita.setUsuario(nuevoUsuario);
        citaRepository.save(nuevaCita);

        // 3. Al terminar, mandamos al usuario a una página de éxito
        return "redirect:/exito.html";
    }
}