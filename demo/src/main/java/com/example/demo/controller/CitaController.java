package com.example.demo.controller;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.Map;
import java.util.List;
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
public String mostrarPanel(Model model) {
    // 1. Esto es lo que ya tienes y hace que se vea la fila de "Kevin Ambel"
    List<Cita> listaCitas = citaRepository.findAll();
    model.addAttribute("citas", listaCitas);
    
    // 2. ESTO ES LO QUE TE FALTA:
    // Tienes que traer los datos de la tabla disponibilidad y pasarlos como "huecos"
    List<Disponibilidad> listaHuecos = disponibilidadRepository.findAll();
    model.addAttribute("huecos", listaHuecos);
    
    return "panel_admin"; 
}

@PostMapping("/admin/eliminar-hueco")
public String eliminarHueco(@RequestParam("id") Long id) {
    // Borramos el hueco de la base de datos usando su ID
    disponibilidadRepository.deleteById(id);
    
    // Volvemos al panel para que el hueco ya no aparezca
    return "redirect:/admin";
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
        // 1. Buscamos el hueco (Aquí es donde se crea la variable 'hueco')
        Disponibilidad hueco = disponibilidadRepository.findById(disponibilidadId)
            .orElseThrow(() -> new RuntimeException("Hueco no encontrado"));

        // 2. Configuramos la cita con los datos del hueco
        cita.setFechaCita(hueco.getFechaHora());
        cita.setDisponibilidad(hueco); 
        cita.setEstado("PENDIENTE");

        // 3. Guardamos la cita y capturamos el objeto con su ID real
        Cita citaGuardada = citaRepository.save(cita); 
        
        // 4. Ahora sí, marcamos el hueco como reservado
        hueco.setReservada(true);
        disponibilidadRepository.save(hueco);

        // 5. Devolvemos el ID de la CITA (no del hueco) al frontend
        return ResponseEntity.ok().body(Map.of(
            "status", "ok",
            "citaId", citaGuardada.getId()
        ));

    } catch (Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
}
}
