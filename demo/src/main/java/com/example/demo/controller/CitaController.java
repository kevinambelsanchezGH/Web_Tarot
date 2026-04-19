package com.example.demo.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

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
@GetMapping("/") // O la ruta que use tu index
public String mostrarIndex(Model model) {
    // Esto es lo que llena la variable ${huecosDisponibles} de tu HTML
    model.addAttribute("huecosDisponibles", disponibilidadRepository.findByReservadaFalse());
    return "index"; 
}
@PostMapping("/admin/eliminar-cita")
public String eliminarCita(@RequestParam("id") Long id) {
    citaRepository.deleteById(id);
    return "redirect:/admin"; // Al terminar, recarga el panel automáticamente
}
@PostMapping("/guardar")
@ResponseBody
public ResponseEntity<?> guardarCita(@ModelAttribute Cita cita, @RequestParam Long disponibilidadId) {
    try {
        // 1. Buscamos el hueco
        Disponibilidad hueco = disponibilidadRepository.findById(disponibilidadId)
            .orElseThrow(() -> new RuntimeException("Hueco no encontrado"));

        // 2. Le pasamos la FECHA (lo que hicimos antes)
        cita.setFechaCita(hueco.getFechaHora());
        
        // 3. ¡ESTO ES LO NUEVO!: Le pasamos el OBJETO disponibilidad a la cita
        // Asegúrate de que en tu clase Cita el atributo se llame "disponibilidad" o similar
        cita.setDisponibilidad(hueco); 
        
        // 4. Si tienes un campo estado, ponlo como pendiente de pago
        cita.setEstado("PENDIENTE");

        // 5. Guardamos la cita
        citaRepository.save(cita);
        
        // 6. Marcamos el hueco como reservado (mejor que borrarlo por ahora)
        hueco.setReservada(true);
        disponibilidadRepository.save(hueco);

        return ResponseEntity.ok().body(Map.of("status", "ok"));

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
}
}