package com.santi.linea.repositories;

import com.santi.linea.models.EstadoVale;
import com.santi.linea.models.OrdenProduccion;
import com.santi.linea.models.ValeProduccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ValeProduccionRepository extends JpaRepository<ValeProduccion, Long> {
    List<ValeProduccion> findByOrdenProduccion_Id(Long ordenId);
    Optional<ValeProduccion> findByCodigoVale(String codigoVale);
    Optional<ValeProduccion> findFirstByOrdenAndEstado(OrdenProduccion orden, EstadoVale estado);
}