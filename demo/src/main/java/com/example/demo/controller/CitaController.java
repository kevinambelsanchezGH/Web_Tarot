package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.repository.CitaRepository;

import org.springframework.ui.Model;

@Controller
public class CitaController {

    @Autowired
    private CitaRepository citaRepository;

    @GetMapping("/admin")
public String mostrarPanelAdmin(Model model) {
    System.out.println("¡Entrando en el controlador!");
    
    var listaCitas = citaRepository.findAll();
    
    // Añade esta línea:
    System.out.println("Número de registros encontrados: " + listaCitas.size());
    
    model.addAttribute("citas", listaCitas);
    return "panel_admin"; 
}
}