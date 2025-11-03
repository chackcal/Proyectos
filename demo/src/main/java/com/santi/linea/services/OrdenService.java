package com.santi.linea.services;

import com.santi.linea.dto.CrearOrdenRequest;
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
    public OrdenProduccion crearOrden(CrearOrdenRequest req) {
        if (req.getProductoFinalId() == null || req.getFormulaId() == null || req.getCantidad() == null || req.getCantidad() <= 0) {
            throw new IllegalArgumentException("Datos inválidos: producto, formula y cantidad > 0 son obligatorios.");
        }

        var producto = productoRepo.findById(req.getProductoFinalId())
                .orElseThrow(() -> new IllegalArgumentException("Producto final no existe"));

        var formula = formulaRepo.findById(req.getFormulaId())
                .orElseThrow(() -> new IllegalArgumentException("Fórmula no existe"));

        // (Opcional) Validar que la fórmula corresponde al producto final
        if (formula.getProductoFinal() != null && !formula.getProductoFinal().getId().equals(producto.getId())) {
            throw new IllegalArgumentException("La fórmula seleccionada no corresponde al producto final.");
        }

        // Código único
        String codigo = (req.getCodigoOrden() == null || req.getCodigoOrden().isBlank())
                ? generarCodigoOrden(producto) // helper tuyo o uno simple abajo
                : req.getCodigoOrden().trim();

        // Verificar unicidad
        if (ordenRepo.existsByCodigoOrden(codigo)) {
            throw new IllegalArgumentException("El código de orden ya existe: " + codigo);
        }

        var op = OrdenProduccion.builder()
                .codigoOrden(codigo)
                .productoFinal(producto)
                .formula(formula)
                .cantidad(req.getCantidad())
                .producidas(0)
                .estado(EstadoOrden.EN_PROCESO)
                .fechaInicio(req.getFechaInicio() != null && !req.getFechaInicio().isBlank()
                        ? java.time.LocalDate.parse(req.getFechaInicio())
                        : java.time.LocalDate.now())
                .build();

        return ordenRepo.saveAndFlush(op);
    }

    private String generarCodigoOrden(Producto producto) {
        // Ej: OP-NB001-20251103-0001 (puedes usar tu CodeGenerator si quieres)
        String pref = "OP-" + (producto.getCodigo() != null ? producto.getCodigo() : "GEN");
        String date = java.time.LocalDate.now().toString().replaceAll("-", "");
        long countHoy = ordenRepo.count(); // simple; puedes contar por día si prefieres
        return String.format("%s-%s-%04d", pref, date, (countHoy % 10000));
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
            ordenRepo.saveAndFlush(op);
        }
    }

    // OrdenService.java
    public List<OrdenProduccion> finalizadas() {
        return ordenRepo.findByEstadoOrderByFechaFinDesc(EstadoOrden.FINALIZADA);
    }

    public Optional<OrdenProduccion> findById(Long id) {
        return ordenRepo.findById(id);
    }
}
