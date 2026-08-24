package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;

// Esta clase configura la seguridad de toda la aplicación: qué rutas son públicas,
// cuáles requieren estar logueado, y quién puede entrar al panel de administración.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // Este bean define las reglas de acceso (el "firewall" de Spring Security).
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
           .csrf(csrf -> csrf
            .ignoringRequestMatchers("/guardar", "/crear-sesion-pago"))
            .authorizeHttpRequests(auth -> auth
                // 1. RUTAS PÚBLICAS: Todo el mundo puede ver la web, el CSS y hacer el proceso de pago
                .requestMatchers("/", "/css/**", "/js/**", "/favicon.ico", "/error", "/sobre-mi.html", "/login").permitAll()
                .requestMatchers("/guardar", "/crear-sesion-pago", "/pago-exito", "/pago-cancelado").permitAll()
                // 2. RUTAS PRIVADAS: Solo tú (con rol ADMIN) puedes entrar a cualquier cosa que empiece por /admin
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // 3. SEGURIDAD EXTRA: Cualquier otra ruta no definida requerirá estar logueado
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login") // Usamos nuestra propia página de login en español
                .permitAll()
            )
            .logout(logout -> logout.logoutSuccessUrl("/")); // Al cerrar sesión, vuelve al inicio

        // Construye y devuelve la cadena de filtros de seguridad ya configurada
        return http.build();
    }

    // Este bean crea el (único) usuario administrador, guardado en memoria (no en la base de datos).
    @Value("${admin.username}")
private String adminUsername;

@Value("${admin.password}")
private String adminPassword;

@Bean
public InMemoryUserDetailsManager userDetailsService() {
    UserDetails user = User.builder()
        .username(adminUsername)
        .password("{noop}" + adminPassword)
        .roles("ADMIN")
        .build();
    return new InMemoryUserDetailsManager(user);
}
}
