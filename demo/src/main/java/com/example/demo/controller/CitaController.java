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
import com.example.demo.repository.PagoRepository;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.DisponibilidadRepository;

import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;

// Controlador principal: gestiona la página pública (huecos disponibles, guardar cita)
// y el panel de administración (ver citas, añadir/eliminar huecos, eliminar citas).
@Controller
public class CitaController {

    // Acceso a la tabla de citas
    @Autowired
    private CitaRepository citaRepository;
    // Acceso a la tabla de huecos disponibles
    @Autowired
    private DisponibilidadRepository disponibilidadRepository;
    // Acceso a la tabla de pagos (se usa para borrar el pago ligado a una cita)
    @Autowired
    private PagoRepository pagoRepository;


// Muestra el panel de administración con la lista de citas y de huecos.
@GetMapping("/admin")
public String mostrarPanel(Model model) {

    // Saco todas las citas de la base de datos y se las paso a la vista
    List<Cita> listaCitas = citaRepository.findAll();
    model.addAttribute("citas", listaCitas);


    // Saco todos los huecos (reservados o no) y se los paso a la vista
    List<Disponibilidad> listaHuecos = disponibilidadRepository.findAll();
    model.addAttribute("huecos", listaHuecos);

    // Renderiza panel_admin.html con esos datos
    return "panel_admin";
}

// Borra un hueco de disponibilidad a partir de su id (botón "eliminar" del admin).
@PostMapping("/admin/eliminar-hueco")
public String eliminarHueco(@RequestParam("id") Long id) {
    // Borramos el hueco de la base de datos usando su ID
    disponibilidadRepository.deleteById(id);

    // Volvemos al panel para que el hueco ya no aparezca
    return "redirect:/admin";
}

// Crea un nuevo hueco libre a partir de la fecha/hora que introduce el admin.
@PostMapping("/admin/agregar-fecha")
public String agregarFecha(@RequestParam("fechaHora") String fechaHora) {
    Disponibilidad d = new Disponibilidad();
    // Convierto el texto recibido (formato ISO) en un LocalDateTime real
    d.setFechaHora(LocalDateTime.parse(fechaHora));
    d.setReservada(false); // Siempre empieza libre
    disponibilidadRepository.save(d);
    return "redirect:/admin";
}

// Página de inicio: muestra los huecos que todavía están libres para que el cliente elija uno.
@GetMapping("/")
public String mostrarIndex(Model model) {
    // Esto es lo que llena la variable ${huecosDisponibles} del HTML
    model.addAttribute("huecosDisponibles", disponibilidadRepository.findByReservadaFalse());
    return "index";
}

// Página de login personalizada del panel de administración
@GetMapping("/login")
public String mostrarLogin() {
    return "login";
}

// Elimina una cita y, antes, su pago asociado (si lo tuviera), para no romper la relación en BD.
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

// Recibe el formulario de reserva del cliente y crea la cita, bloqueando el hueco elegido
// para que dos personas no puedan reservar el mismo hueco a la vez.
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
            // Si ya estaba cogido, devuelvo un 409 (conflicto) y no creo la cita
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
        // Cualquier fallo (hueco no encontrado, error de BD, etc.) se devuelve como error 500
        e.printStackTrace();
        return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
}
}
