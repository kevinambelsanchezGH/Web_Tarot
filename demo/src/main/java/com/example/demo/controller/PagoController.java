package com.example.demo.controller;

import com.example.demo.model.Cita;
import com.example.demo.model.Pago;
import com.example.demo.repository.CitaRepository;
import com.example.demo.repository.PagoRepository;
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
            .setSuccessUrl("http://localhost:8080/pago-exito?citaId=" + citaId)
            .setCancelUrl("http://localhost:8080/pago-cancelado")
            .addLineItem(SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency("eur")
                    .setUnitAmount(7000L) // 70,00€
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
    @GetMapping("/pago-exito")
    public String pagoExito(@RequestParam(required = false) String citaId, Model model) {
        System.out.println("!!! ATENCION: Stripe ha vuelto !!!");
        System.out.println("Valor de citaId que recibo de la URL: " + citaId);

        // Si Stripe no manda citaId (o viene vacío), no hay nada que procesar: vuelvo al inicio
        if (citaId == null || citaId.isEmpty()) {
            return "redirect:/";
        }

        try {
            // El citaId llega como texto desde la URL, lo convierto a número para buscar la cita
            Long id = Long.parseLong(citaId);
            Optional<Cita> citaOpt = citaRepository.findById(id);

            if (citaOpt.isPresent()) {
                Cita cita = citaOpt.get();
                cita.setEstado("PAGADO"); // Cambiamos estado
                citaRepository.save(cita); // Guardamos en BD

                // Creamos y guardamos el objeto Pago
                Pago pago = new Pago();
                pago.setCita(cita);
                pago.setImporte(70.00); // El importe es 70.00€ según la configuración de Stripe (7000L cents)
                pago.setMetodoPago("Stripe Card"); // Se puede mejorar obteniendo el método de pago real de Stripe
                pago.setFechaPago(LocalDateTime.now());
                pago.setEstado("COMPLETADO");
                pagoRepository.save(pago);

                System.out.println("EXITO: Cita " + id + " actualizada a PAGADO");
                // Le paso el nombre del cliente a la vista para mostrar un mensaje personalizado
                model.addAttribute("nombre", cita.getNombre());
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
