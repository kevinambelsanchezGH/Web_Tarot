package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

// Clase de arranque de la aplicación Spring Boot.
// @EnableScheduling activa las tareas periódicas (como la que expira reservas sin pagar).
@SpringBootApplication
@EnableScheduling
public class DemoApplication {
    // Punto de entrada: levanta el servidor y todo el contexto de Spring
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
