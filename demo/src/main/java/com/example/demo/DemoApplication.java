package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Clase de arranque de la aplicación Spring Boot.
@SpringBootApplication
public class DemoApplication {
    // Punto de entrada: levanta el servidor y todo el contexto de Spring
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
