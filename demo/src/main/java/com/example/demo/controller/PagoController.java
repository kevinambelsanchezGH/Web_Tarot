package com.example.demo.controller;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class PagoController {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostMapping("/crear-sesion-pago")
    @ResponseBody
    public Map<String, String> crearSesion(@RequestParam Long citaId) {
        Stripe.apiKey = stripeApiKey;

        // Aquí configuramos la página de Stripe que verá el cliente
        SessionCreateParams params = SessionCreateParams.builder()
        .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
            .setMode(SessionCreateParams.Mode.PAYMENT)
            // Cuando el pago sea OK, vuelve a tu web. Cambia localhost por tu dominio real luego.
            .setSuccessUrl("http://localhost:8080/pago-exito?citaId=" + citaId)
            .setCancelUrl("http://localhost:8080/pago-cancelado")
            .addLineItem(SessionCreateParams.LineItem.builder()
                .setQuantity(1L)
                .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                    .setCurrency("eur")
                    .setUnitAmount(7000L) // 50,00€ (se pone en céntimos)
                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("Reserva de Cita - Tarot")
                        .build())
                    .build())
                .build())
            .build();

        Map<String, String> response = new HashMap<>();
        try {
            Session session = Session.create(params);
            response.put("url", session.getUrl()); // Devolvemos la URL de Stripe
        } catch (StripeException e) {
            response.put("error", e.getMessage());
        }
        return response;
    }
}