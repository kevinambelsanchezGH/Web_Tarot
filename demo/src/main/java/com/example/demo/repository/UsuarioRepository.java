package com.example.demo.repository;

import com.example.demo.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Repositorio de usuarios. De momento solo usa las operaciones básicas heredadas de JpaRepository.
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
