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
import java.util.Optional;
import java.util.Map;
import java.util.List;
import com.example.demo.model.Cita;
import com.example.demo.model.Disponibilidad;
import com.example.demo.repository.PagoRepository;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.DisponibilidadRepository;

import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;

@Controller
public class CitaController {

    @Autowired
    private CitaRepository citaRepository;
    @Autowired
    private DisponibilidadRepository disponibilidadRepository;
    @Autowired
    private PagoRepository pagoRepository;


@GetMapping("/admin")
public String mostrarPanel(Model model) {
   
    List<Cita> listaCitas = citaRepository.findAll();
    model.addAttribute("citas", listaCitas);
    
 
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
@GetMapping("/") 
public String mostrarIndex(Model model) {
    // Esto es lo que llena la variable ${huecosDisponibles} del HTML
    model.addAttribute("huecosDisponibles", disponibilidadRepository.findByReservadaFalse());
    return "index"; 
}

@Transactional
@PostMapping("/admin/eliminar-cita")
public String eliminarCita(@RequestParam("id") Long id) {
    // 1. Borramos primero el pago asociado para que la base de datos nos deje borrar la cita
    // (Asegúrate de que en PagoRepository.java tengas el método: void deleteByCitaId(Long id);)
    pagoRepository.deleteByCitaId(id);

    // 2. Ahora borramos la cita directamente por su ID
    citaRepository.deleteById(id);

    return "redirect:/admin"; 
}
@PostMapping("/guardar")
@ResponseBody
@Transactional // 1 sola transacción a la vez
public ResponseEntity<?> guardarCita(@ModelAttribute Cita cita, @RequestParam Long disponibilidadId) {
    try {
        // 1. Busco el hueco con bloqueo pesimista. Bloquea la fila hasta que este método termine.
        Disponibilidad hueco = disponibilidadRepository.findByIdWithLock(disponibilidadId)
            .orElseThrow(() -> new RuntimeException("Hueco no encontrado"));

        // 2. VERIFICO si el hueco ya está reservado *después* de adquirir el lock.
        if (hueco.isReservada()) {
            return ResponseEntity.status(409).body(Map.of("error", "El hueco ya ha sido reservado por otro usuario."));
        }
        // 2. Configuro la cita con los datos del hueco
        cita.setFechaCita(hueco.getFechaHora());
        cita.setDisponibilidad(hueco); 
        cita.setEstado("PENDIENTE");

        // 3. Guardo la cita y capturo el objeto con su ID real
        Cita citaGuardada = citaRepository.save(cita); 
        
        // 4. Ahora sí, marco el hueco como reservado
        hueco.setReservada(true);
        disponibilidadRepository.save(hueco);

        // 5. Devuelvo el ID de la CITA (no del hueco) al frontend
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
