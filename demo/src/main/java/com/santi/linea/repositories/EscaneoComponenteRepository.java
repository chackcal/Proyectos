package com.santi.linea.repositories;

import com.santi.linea.models.EscaneoComponente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EscaneoComponenteRepository extends JpaRepository<EscaneoComponente, Long> {
    long countByVale_IdAndPuesto_Id(Long valeId, Long puestoId);
    Optional<EscaneoComponente> findTopByVale_IdAndPuesto_IdOrderByFechaHoraDesc(Long valeId, Long puestoId);
    List<EscaneoComponente> findByVale_IdAndPuesto_Id(Long valeId, Long puestoId);
}
