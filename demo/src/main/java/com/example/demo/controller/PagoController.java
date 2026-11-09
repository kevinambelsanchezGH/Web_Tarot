package com.example.demo.controller;

import com.example.demo.model.Cita;
import com.example.demo.model.Disponibilidad;
import com.example.demo.model.Pago;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.DisponibilidadRepository;
import com.example.demo.repository.PagoRepository;
import com.example.demo.service.EmailService;
import com.example.demo.service.GoogleCalendarService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.time.LocalDateTime;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

// Controlador que gestiona todo el proceso de pago con Stripe:
// crea la sesión de pago y procesa la vuelta (éxito) desde Stripe.
@Controller
public class PagoController {

    // Acceso a la tabla de citas, para comprobar que la cita existe y marcarla como pagada
    @Autowired
    private CitaRepository citaRepository;

    // Acceso a la tabla de pagos, para guardar el registro del pago realizado
    @Autowired
    private PagoRepository pagoRepository;

    // Acceso a la tabla de huecos, para volver a marcar el hueco como reservado
    // si el pago llega tarde (después de que el job de limpieza lo liberase)
    @Autowired
    private DisponibilidadRepository disponibilidadRepository;

    // Envía el email de confirmación de cita al cliente cuando el pago se completa
    @Autowired
    private EmailService emailService;

    // Crea el evento en el Google Calendar compartido cuando el pago se completa
    @Autowired
    private GoogleCalendarService googleCalendarService;

    // Estado de una cita que se pagó tarde cuando su hueco ya lo tenía otra persona (o se borró).
    // El cliente ha pagado pero no tiene hora: hay que devolverle el dinero o darle otra hora.
    public static final String ESTADO_SIN_HUECO = "PAGADO_SIN_HUECO";

    // Clave secreta de la API de Stripe, leída de application.properties
    @Value("${stripe.api.key}")
    private String stripeApiKey;

    // Crea la sesión de pago de Stripe (Checkout) para una cita concreta y devuelve la URL a la que redirigir al cliente.
    @PostMapping("/crear-sesion-pago")
    @ResponseBody
    public Map<String, String> crearSesion(@RequestParam Long citaId) {
        // Configuro la librería de Stripe con nuestra clave secreta
        Stripe.apiKey = stripeApiKey;

        // Busco la cita real para asegurarme de que el ID que mando a Stripe existe
        Optional<Cita> citaCheck = citaRepository.findById(citaId);
        if (citaCheck.isEmpty()) {
            // Si la cita no existe, devuelvo un mensaje de error en vez de crear la sesión
            Map<String, String> errorRes = new HashMap<>();
            errorRes.put("error", "La cita con ID " + citaId + " no existe en la base de datos");
            return errorRes;
        }
        // -------------------------------------

        // Construyo los parámetros de la sesión de Stripe: método de pago, precio, y a dónde
        // redirigir según si el pago sale bien o se cancela
        SessionCreateParams params = SessionCreateParams.builder()
            .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
            .setMode(SessionCreateParams.Mode.PAYMENT)
            .setClientReferenceId(citaId.toString())
            // Por defecto Stripe deja la página de pago abierta 24 h. La cerramos lo antes que
            // permite Stripe (mínimo 30 min; ponemos 31 para no rozar el límite por desfase de
            // reloj). La reserva en la web sigue caducando a los 10 min; un pago que entre entre
            // el minuto 10 y el 31 se trata como "pago tardío" en pagoExito.
            .setExpiresAt(java.time.Instant.now().plusSeconds(31 * 60).getEpochSecond())
            .setSuccessUrl("https://dualtarot.es/pago-exito?citaId=" + citaId + "&session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl("https://dualtarot.es/pago-cancelado")
            .addLineItem(SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency("eur")
                    .setUnitAmount(7500L) // 75,00€
                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("Reserva de Cita - Tarot")
                        .build())
                    .build())
                .build())
            .build();

        Map<String, String> response = new HashMap<>();
        try {
            // Le pido a Stripe que cree la sesión y devuelvo su URL de pago al frontend
            Session session = Session.create(params);
            response.put("url", session.getUrl());
        } catch (StripeException e) {
            // Si Stripe falla (clave inválida, red, etc.), devuelvo el error en vez de la URL
            response.put("error", e.getMessage());
        }
        return response;
    }

    // Página a la que Stripe redirige tras un pago correcto: marca la cita como pagada y guarda el pago.
    // @Transactional porque, si el hueco había sido liberado por LimpiezaReservasService, aquí lo
    // volvemos a bloquear con findByIdWithLock (bloqueo pesimista, requiere una transacción activa).
    @GetMapping("/pago-exito")
    @Transactional
    public String pagoExito(@RequestParam(required = false) String citaId,
                             @RequestParam(required = false) String session_id,
                             Model model) {
        System.out.println("!!! ATENCION: Stripe ha vuelto !!!");
        System.out.println("Valor de citaId que recibo de la URL: " + citaId);

        // Si Stripe no manda citaId o session_id (o vienen vacíos), no hay nada que procesar: vuelvo al inicio
        if (citaId == null || citaId.isEmpty() || session_id == null || session_id.isEmpty()) {
            return "redirect:/";
        }

        try {
            // El citaId llega como texto desde la URL, lo convierto a número para buscar la cita
            Long id = Long.parseLong(citaId);
            Optional<Cita> citaOpt = citaRepository.findById(id);

            if (citaOpt.isPresent()) {
                Cita cita = citaOpt.get();

                // Verificamos contra la propia API de Stripe que esta sesión de pago concreta está
                // realmente pagada y corresponde a esta cita, antes de tocar nada en la base de datos.
                // Así no basta con conocer/adivinar un citaId para forzar el estado PAGADO: hace falta
                // un session_id que Stripe confirme como pagado, y eso solo lo genera un pago real.
                Stripe.apiKey = stripeApiKey;
                Session stripeSession;
                try {
                    stripeSession = Session.retrieve(session_id);
                } catch (StripeException e) {
                    System.out.println("ERROR: session_id inválido al verificar el pago: " + e.getMessage());
                    return "redirect:/";
                }
                boolean pagoValido = "paid".equals(stripeSession.getPaymentStatus())
                        && citaId.equals(stripeSession.getClientReferenceId());
                if (!pagoValido) {
                    System.out.println("ERROR: Stripe no confirma el pago de la cita " + id + " para la sesión " + session_id);
                    return "redirect:/";
                }

                model.addAttribute("nombre", cita.getNombre());

                // Si ya se procesó este pago (p. ej. el cliente recarga esta misma página), no
                // repetimos nada: ni otro registro de Pago, ni email, ni evento de calendario.
                if ("PAGADO".equals(cita.getEstado())) {
                    return "pago_exito";
                }
                if (ESTADO_SIN_HUECO.equals(cita.getEstado())) {
                    model.addAttribute("sinHueco", true);
                    return "pago_exito";
                }

                // Pago tardío: la cita ya había caducado (EXPIRADA) y su hueco se liberó.
                // Solo se lo devolvemos si sigue existiendo y nadie más lo ha reservado.
                boolean sinHueco = false;
                if ("EXPIRADA".equals(cita.getEstado())) {
                    Disponibilidad hueco = cita.getDisponibilidad() == null ? null
                            : disponibilidadRepository.findByIdWithLock(cita.getDisponibilidad().getId()).orElse(null);
                    if (hueco == null || hueco.isReservada()) {
                        // Otra persona lo cogió (o el admin borró el hueco): el cliente ha pagado
                        // pero no tiene hora. Se marca para gestionarlo a mano desde el panel.
                        sinHueco = true;
                    } else {
                        hueco.setReservada(true);
                        disponibilidadRepository.save(hueco);
                    }
                }

                cita.setEstado(sinHueco ? ESTADO_SIN_HUECO : "PAGADO");
                citaRepository.save(cita);

                // Creamos y guardamos el objeto Pago (también en el caso sin hueco: el dinero se cobró)
                Pago pago = new Pago();
                pago.setCita(cita);
                pago.setImporte(75.00); // El importe es 75.00€ según la configuración de Stripe (7500L cents)
                pago.setMetodoPago("Stripe Card"); // Se puede mejorar obteniendo el método de pago real de Stripe
                pago.setFechaPago(LocalDateTime.now());
                pago.setEstado("COMPLETADO");
                pagoRepository.save(pago);

                if (sinHueco) {
                    // No mandamos confirmación ni creamos evento: esa hora no es suya.
                    System.out.println("AVISO: Cita " + id + " pagada tarde y su hueco ya no está disponible -> " + ESTADO_SIN_HUECO);
                    model.addAttribute("sinHueco", true);
                    return "pago_exito";
                }

                emailService.enviarConfirmacionCita(cita);
                googleCalendarService.crearEventoCita(cita);

                System.out.println("EXITO: Cita " + id + " actualizada a PAGADO");
                return "pago_exito"; // Carga pago_exito.html
            } else {
                // Si el id no corresponde a ninguna cita, no hay nada que mostrar: vuelvo al inicio
                System.out.println("ERROR: No existe la cita con ID: " + id);
                return "redirect:/";
            }
        } catch (NumberFormatException e) {
            // Si citaId no es un número válido, evito que la app explote y vuelvo al inicio
            return "redirect:/";
        }
    }
}
