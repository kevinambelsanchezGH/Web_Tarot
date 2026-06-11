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

@Controller
public class PagoController {

    @Autowired
    private CitaRepository citaRepository;
    
    @Autowired
    private PagoRepository pagoRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostMapping("/crear-sesion-pago")
    @ResponseBody
    public Map<String, String> crearSesion(@RequestParam Long citaId) {
        Stripe.apiKey = stripeApiKey;

        // Busco la cita real para asegurarme de que el ID que mando a Stripe existe
        Optional<Cita> citaCheck = citaRepository.findById(citaId);
        if (citaCheck.isEmpty()) {
            Map<String, String> errorRes = new HashMap<>();
            errorRes.put("error", "La cita con ID " + citaId + " no existe en la base de datos");
            return errorRes;
        }
        // -------------------------------------

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
            Session session = Session.create(params);
            response.put("url", session.getUrl());
        } catch (StripeException e) {
            response.put("error", e.getMessage());
        }
        return response;
    }

    @GetMapping("/pago-exito")
    public String pagoExito(@RequestParam(required = false) String citaId, Model model) {
        System.out.println("!!! ATENCION: Stripe ha vuelto !!!");
        System.out.println("Valor de citaId que recibo de la URL: " + citaId);

        if (citaId == null || citaId.isEmpty()) {
            return "redirect:/";
        }

        try {
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
                model.addAttribute("nombre", cita.getNombre());
                return "pago_exito"; // Carga pago_exito.html
            } else {
                System.out.println("ERROR: No existe la cita con ID: " + id);
                return "redirect:/";
            }
        } catch (NumberFormatException e) {
            return "redirect:/";
        }
    }
}
