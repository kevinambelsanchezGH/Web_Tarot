package com.example.demo.repository;

import com.example.demo.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Repositorio de pagos.
@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    /**
     * Borra el registro de pago asociado a una cita específica.
     * Spring Data JPA generará la consulta SQL automáticamente.
     */
    void deleteByCitaId(Long citaId);
}
