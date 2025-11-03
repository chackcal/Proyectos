package com.santi.linea.repositories;

import com.santi.linea.models.FormulaPuestoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormulaPuestoDetalleRepository extends JpaRepository<FormulaPuestoDetalle, Long> {
    List<FormulaPuestoDetalle> findByFormulaIdAndPuestoId(Long formulaId, Long puestoId);
}
