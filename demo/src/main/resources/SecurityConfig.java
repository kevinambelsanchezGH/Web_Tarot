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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF para que tus formularios de admin sigan funcionando fácil
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").hasRole("ADMIN") // Solo el admin requiere login y el rol ADMIN
                .anyRequest().permitAll() // Todo lo demás (index, pago, css) es público
            )
            .formLogin(withDefaults()) // Usa el formulario de login por defecto de Spring
            .logout(logout -> logout.logoutSuccessUrl("/")); // Al cerrar sesión, vuelve al inicio
        
        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        // Usamos {noop} para contraseñas en texto plano (ideal para TFG/Desarrollo)
        UserDetails user = User.builder()
            .username("***REMOVED***")
            .password("{noop}***REMOVED***")
            .roles("ADMIN")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}
