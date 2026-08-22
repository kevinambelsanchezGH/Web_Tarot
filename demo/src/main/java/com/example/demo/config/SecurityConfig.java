package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import static org.springframework.security.config.Customizer.withDefaults;

// Esta clase configura la seguridad de toda la aplicación: qué rutas son públicas,
// cuáles requieren estar logueado, y quién puede entrar al panel de administración.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Este bean define las reglas de acceso (el "firewall" de Spring Security).
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF para que tus formularios de admin sigan funcionando fácil
            .authorizeHttpRequests(auth -> auth
                // 1. RUTAS PÚBLICAS: Todo el mundo puede ver la web, el CSS y hacer el proceso de pago
                .requestMatchers("/", "/css/**", "/js/**", "/favicon.ico", "/error", "/sobre-mi.html").permitAll()
                .requestMatchers("/guardar", "/crear-sesion-pago", "/pago-exito", "/pago-cancelado").permitAll()
                // 2. RUTAS PRIVADAS: Solo tú (con rol ADMIN) puedes entrar a cualquier cosa que empiece por /admin
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 3. SEGURIDAD EXTRA: Cualquier otra ruta no definida requerirá estar logueado
                .anyRequest().authenticated()
            )
            .formLogin(withDefaults()) // Usa el formulario de login por defecto de Spring
            .logout(logout -> logout.logoutSuccessUrl("/")); // Al cerrar sesión, vuelve al inicio

        // Construye y devuelve la cadena de filtros de seguridad ya configurada
        return http.build();
    }

    // Este bean crea el (único) usuario administrador, guardado en memoria (no en la base de datos).
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        // Usamos {noop} para contraseñas en texto plano (ideal para TFG/Desarrollo)
        UserDetails user = User.builder()
            .username("***REMOVED***")
            .password("{noop}***REMOVED***")
            .roles("ADMIN")
            .build();
        // Registra ese único usuario en un gestor de usuarios en memoria
        return new InMemoryUserDetailsManager(user);
    }
}
