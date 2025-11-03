package com.santi.linea.services;

import com.santi.linea.models.*;
import com.santi.linea.repositories.*;
import com.santi.linea.utils.CodeGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ProduccionService {

    private final ValeProduccionRepository valeRepo;
    private final OrdenProduccionRepository ordenRepo;
    private final PuestoRepository puestoRepo;
    private final ProductoRepository productoRepo;
    private final EscaneoComponenteRepository escaneoRepo;
    private final FormulaRepository formulaRepo;
    private final FormulaPuestoDetalleRepository formulaPuestoRepo;
    private final OrdenService ordenService;
    private final com.santi.linea.utils.CodeGenerator codeGenerator;
    private final ValePuestoRequisitoRepository vprRepo;

    @Transactional
    public ValeProduccion iniciarVale(Long opId) {
        var op = ordenRepo.findById(opId).orElseThrow();
        var codigo = codeGenerator.nextVale(op.getCodigoOrden());

        var vale = valeRepo.save(ValeProduccion.builder()
                .ordenProduccion(op)
                .codigoVale(codigo)
                .estado(EstadoVale.EN_PROCESO)
                .puestoActual(1)
                .build());

        // sembrar requisitos => una fila por componente requerido en cada puesto
        var puestos = puestoRepo.findAllByOrderByOrdenSecuenciaAsc();
        for (var p : puestos) {
            var detalles = formulaPuestoRepo.findByFormulaIdAndPuestoId(op.getFormula().getId(), p.getId());
            // agrupar por componente
            Map<Long,Integer> sumas = new HashMap<>();
            for (var d : detalles) {
                sumas.merge(d.getComponente().getId(), d.getCantidad(), Integer::sum);
            }
            for (var entry : sumas.entrySet()) {
                var comp = productoRepo.findById(entry.getKey()).orElseThrow();
                vprRepo.save(ValePuestoRequisito.builder()
                        .vale(vale).puesto(p).componente(comp)
                        .requerido(entry.getValue())
                        .escaneado(0)
                        .build());
            }
        }
        return vale;
    }

    @Transactional
    public void escanearComponente(String codigoVale, int puestoSecuencia, String codigoComponente) {
        ValeProduccion vale = valeRepo.findByCodigoVale(codigoVale).orElseThrow();
        if (!vale.getEstado().equals(EstadoVale.EN_PROCESO)) throw new IllegalStateException("Vale no activo.");
        if (!Objects.equals(vale.getPuestoActual(), puestoSecuencia)) throw new IllegalStateException("Puesto incorrecto.");

        Puesto puesto = puestoRepo.findAllByOrderByOrdenSecuenciaAsc().stream()
                .filter(p -> p.getOrdenSecuencia() == puestoSecuencia).findFirst().orElseThrow();

        // Localizar componente por código (si sabés el tipo podés usar findByTipoAndCodigo)
        Producto comp = productoRepo.findByCodigo(codigoComponente)
                .orElseThrow(() -> new IllegalArgumentException("Componente inexistente"));

        // Validar que este componente pertenece a la fórmula **en este puesto**:
        Long formulaId = vale.getOrdenProduccion().getFormula().getId();
        var requeridosPuesto = formulaPuestoRepo.findByFormulaIdAndPuestoId(formulaId, puesto.getId());

        Optional<FormulaPuestoDetalle> linea = requeridosPuesto.stream()
                .filter(d -> Objects.equals(d.getComponente().getId(), comp.getId()))
                .findFirst();

        if (linea.isEmpty()) throw new IllegalStateException("Componente no pertenece a este puesto según la fórmula.");

        // Registrar escaneo
        escaneoRepo.save(EscaneoComponente.builder()
                .vale(vale).puesto(puesto).componente(comp)
                .codigoEscaneado(codigoComponente).fechaHora(LocalDateTime.now()).build());

        // ¿Ya cumplimos TODOS los requeridos de este puesto?
        // Contamos por componente, respetando cantidad por unidad (sumatoria de 4 hijos por puesto)
        boolean completo = true;
        for (var det : requeridosPuesto) {
            long qtyEscaneadaDeEseComponente = escaneoRepo.findByVale_IdAndPuesto_Id(vale.getId(), puesto.getId()).stream()
                    .filter(e -> Objects.equals(e.getComponente().getId(), det.getComponente().getId()))
                    .count();
            if (qtyEscaneadaDeEseComponente < det.getCantidad()) {
                completo = false; break;
            }
        }

        if (completo) {
            // Avanzar o finalizar
            int totalPuestos = puestoRepo.findAll().size();
            if (puestoSecuencia < totalPuestos) {
                vale.setPuestoActual(puestoSecuencia + 1);
                valeRepo.save(vale);
            } else {
                vale.setEstado(EstadoVale.FINALIZADO);
                vale.setFinalizadoEn(LocalDateTime.now());
                valeRepo.save(vale);

                OrdenProduccion op = vale.getOrdenProduccion();
                op.setProducidas(op.getProducidas() + 1);
                ordenRepo.save(op);
                ordenService.cerrarSiCompleta(op.getId());
            }
        }
    }

    @Transactional
    public void deshacerUltimoEscaneo(String codigoVale, int puestoSecuencia) {
        ValeProduccion vale = valeRepo.findByCodigoVale(codigoVale).orElseThrow();
        Puesto puesto = puestoRepo.findAllByOrderByOrdenSecuenciaAsc().stream()
                .filter(p -> p.getOrdenSecuencia() == puestoSecuencia).findFirst().orElseThrow();

        var ultimo = escaneoRepo.findTopByVale_IdAndPuesto_IdOrderByFechaHoraDesc(vale.getId(), puesto.getId())
                .orElseThrow(() -> new IllegalStateException("No hay escaneos para deshacer en este puesto."));
        escaneoRepo.delete(ultimo);
    }

    public List<ValeProduccion> valesDeOP(Long opId) { return valeRepo.findByOrdenProduccion_Id(opId); }



    @Transactional
    public Map<String,Object> escanear(String codigoVale, int puestoSec, String codigoComp) {
        var vale = valeRepo.findByCodigoVale(codigoVale).orElseThrow();
        if (vale.getEstado() != EstadoVale.EN_PROCESO)
            return Map.of("ok", false, "error", "Vale no activo");

        if (!Objects.equals(vale.getPuestoActual(), puestoSec))
            return Map.of("ok", false, "error", "Puesto incorrecto para este vale");

        var puesto = puestoRepo.findAllByOrderByOrdenSecuenciaAsc().stream()
                .filter(p -> p.getOrdenSecuencia() == puestoSec).findFirst().orElseThrow();

        var comp = productoRepo.findByCodigo(codigoComp)
                .orElseThrow(() -> new IllegalArgumentException("Componente inexistente"));

        var req = vprRepo.findByVale_IdAndPuesto_IdAndComponente_Id(vale.getId(), puesto.getId(), comp.getId())
                .orElse(null);
        if (req == null) return Map.of("ok", false, "error", "Componente no pertenece a este puesto");

        int updated = vprRepo.tryIncrement(req.getId());
        if (updated == 0)
            return Map.of("ok", false, "error", "Cantidad de este componente ya completa para el puesto");

        // Traza opcional
        escaneoRepo.save(EscaneoComponente.builder()
                .vale(vale).puesto(puesto).componente(comp)
                .codigoEscaneado(codigoComp)
                .fechaHora(java.time.LocalDateTime.now())
                .build());

        boolean completo = vprRepo.isPuestoCompleto(vale.getId(), puesto.getId());

        Map<String,Object> out = new LinkedHashMap<>();
        out.put("ok", true);
        out.put("mensaje", "Escaneo registrado");
        out.put("puestoCompleto", completo);

        // faltantes
        var faltantes = vprRepo.findByVale_IdAndPuesto_Id(vale.getId(), puesto.getId()).stream()
                .filter(r -> r.getEscaneado() < r.getRequerido())
                .map(r -> Map.of(
                        "codigo", r.getComponente().getCodigo(),
                        "componente", r.getComponente().getNombre(),
                        "escaneado", r.getEscaneado(),
                        "requerido", r.getRequerido()
                )).toList();
        out.put("faltantes", faltantes);

        if (completo) {
            int totalPuestos = (int) puestoRepo.count();
            if (puestoSec < totalPuestos) {
                vale.setPuestoActual(puestoSec + 1);
                valeRepo.save(vale);
            } else {
                vale.setEstado(EstadoVale.FINALIZADO);
                valeRepo.save(vale);
            }
        }
        return out;
    }

    @Transactional
    public Map<String,Object> deshacerUltimo(String codigoVale, int puestoSec) {
        var vale = valeRepo.findByCodigoVale(codigoVale).orElseThrow();
        var puesto = puestoRepo.findAllByOrderByOrdenSecuenciaAsc().stream()
                .filter(p -> p.getOrdenSecuencia() == puestoSec).findFirst().orElseThrow();

        var ultimo = escaneoRepo.findTopByVale_IdAndPuesto_IdOrderByFechaHoraDesc(vale.getId(), puesto.getId())
                .orElseThrow(() -> new IllegalStateException("No hay escaneos para deshacer"));

        var req = vprRepo.findByVale_IdAndPuesto_IdAndComponente_Id(
                vale.getId(), puesto.getId(), ultimo.getComponente().getId()
        ).orElseThrow();

        int updated = vprRepo.tryDecrement(req.getId());
        if (updated == 0) return Map.of("ok", false, "error", "Nada para deshacer");

        escaneoRepo.delete(ultimo);
        return Map.of("ok", true, "mensaje", "Escaneo deshecho");
    }

    @Transactional
    public List<Map<String,Object>> checklist(String codigoVale, int puestoSec) {
        var filas = vprRepo.checklist(codigoVale, puestoSec);
        return filas.stream().map(r -> {
            boolean completo = r.getEscaneado() >= r.getRequerido();
            return Map.<String,Object>of(
                    "codigo",    r.getComponente().getCodigo(),
                    "nombre",    r.getComponente().getNombre(),
                    "escaneado", r.getEscaneado(),
                    "requerido", r.getRequerido(),
                    "completo",  completo
            );
        }).toList();
    }
}
