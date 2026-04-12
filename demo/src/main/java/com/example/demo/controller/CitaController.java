package com.example.demo.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.Cita;
import com.example.demo.model.Disponibilidad;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.DisponibilidadRepository;

import org.springframework.ui.Model;

@Controller
public class CitaController {

    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

@GetMapping("/admin")
public String mostrarPanelAdmin(Model model) {
    System.out.println("¡Entrando en el controlador!");
    
    var listaCitas = citaRepository.findAll();
    
  
    System.out.println("Número de registros encontrados: " + listaCitas.size());
    
    model.addAttribute("citas", listaCitas);
    return "panel_admin"; 
    
}

@PostMapping("/admin/agregar-fecha")
public String agregarFecha(@RequestParam("fechaHora") String fechaHora) {
    Disponibilidad d = new Disponibilidad();
    d.setFechaHora(LocalDateTime.parse(fechaHora));
    d.setReservada(false); // Siempre empieza libre
    disponibilidadRepository.save(d);
    return "redirect:/admin";
}
@PostMapping("/citas/guardar")
public String guardarCita(@RequestParam Long disponibilidadId, Cita cita) {
    // 1. Buscamos el hueco elegido
    Disponibilidad hueco = disponibilidadRepository.findById(disponibilidadId).orElseThrow();
    
    // 2. Lo marcamos como ocupado para que desaparezca del desplegable
    hueco.setReservada(true);
    disponibilidadRepository.save(hueco);
    
    // 3. Guardamos la cita asociada al hueco
    cita.setDisponibilidad(hueco); // Asegúrate de tener este set en tu entidad Cita
    citaRepository.save(cita);
    
    return "redirect:/"; // Vuelve al inicio o a una página de confirmación
}
}