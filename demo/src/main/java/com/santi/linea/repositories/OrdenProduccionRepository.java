package com.santi.linea.repositories;

import com.santi.linea.models.EstadoOrden;
import com.santi.linea.models.OrdenProduccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrdenProduccionRepository extends JpaRepository<OrdenProduccion, Long> {
    Optional<OrdenProduccion> findByCodigoOrden(String codigoOrden);
    List<OrdenProduccion> findByEstado(EstadoOrden estado);
}