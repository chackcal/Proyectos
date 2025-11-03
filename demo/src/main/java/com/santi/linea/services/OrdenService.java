package com.santi.linea.services;

import com.santi.linea.models.EstadoOrden;
import com.santi.linea.models.Formula;
import com.santi.linea.models.OrdenProduccion;
import com.santi.linea.models.Producto;
import com.santi.linea.repositories.FormulaRepository;
import com.santi.linea.repositories.OrdenProduccionRepository;
import com.santi.linea.repositories.ProductoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class OrdenService {

    private final OrdenProduccionRepository ordenRepo;
    private final ProductoRepository productoRepo;
    private final FormulaRepository formulaRepo;
    private final com.santi.linea.utils.CodeGenerator codeGenerator;

    @Transactional
    public OrdenProduccion crearOP(Long productoId, Long formulaId, int cantidad) {
        Producto prod = productoRepo.findById(productoId).orElseThrow();
        Formula formula = formulaRepo.findById(formulaId).orElseThrow();

        OrdenProduccion op = OrdenProduccion.builder()
                .codigoOrden(codeGenerator.nextOP())
                .productoFinal(prod)
                .formula(formula)
                .cantidad(cantidad)
                .producidas(0)
                .fechaInicio(LocalDate.now())
                .estado(EstadoOrden.EN_PROCESO)
                .build();
        return ordenRepo.save(op);
    }

    public List<OrdenProduccion> activas(){
        return ordenRepo.findByEstado(EstadoOrden.EN_PROCESO);
    }

    @Transactional
    public void cerrarSiCompleta(Long opId) {
        OrdenProduccion op = ordenRepo.findById(opId).orElseThrow();
        if (op.getProducidas() >= op.getCantidad()) {
            op.setEstado(EstadoOrden.FINALIZADA);
            op.setFechaFin(LocalDate.now());
            ordenRepo.save(op);
        }
    }

    public Optional<OrdenProduccion> findById(Long id) {
        return ordenRepo.findById(id);
    }
}
