package com.santi.linea.controllers;

import com.santi.linea.models.EstadoVale;
import com.santi.linea.models.ValeProduccion;
import com.santi.linea.repositories.ValeProduccionRepository;
import com.santi.linea.services.ProduccionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/produccion")
@CrossOrigin
public class ProduccionController {
    private final ProduccionService service;
    private  ValeProduccionRepository valeRepo;
    public ProduccionController(ProduccionService s){ this.service = s; }

    @PostMapping("/iniciar")
    public ResponseEntity<?> iniciar(@RequestParam("idOrden") Long idOrden) {
        try {
            var vale = service.iniciarVale(idOrden);
            return ResponseEntity.ok(vale);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    // controllers/ProduccionController.java (fragmento)
    @PostMapping("/scan")
    public Map<String,Object> escanear(@RequestParam String codigoVale,
                                       @RequestParam int puesto,
                                       @RequestParam String codigoComponente) {
        return service.escanear(codigoVale, puesto, codigoComponente);
    }

    @PostMapping("/scan/undo")
    public Map<String,Object> deshacer(@RequestParam String codigoVale,
                                       @RequestParam int puesto) {
        return service.deshacerUltimo(codigoVale, puesto);
    }

    // Vales por OP
    @GetMapping("/vales")
    public List<ValeProduccion> valesPorOP(@RequestParam Long opId){
        return service.valesDeOP(opId);
    }

    @GetMapping("/requisitos")
    public List<Map<String,Object>> checklist(@RequestParam String codigoVale,
                                              @RequestParam int puesto) {
        return service.checklist(codigoVale, puesto);
    }
    @GetMapping("/debug/puede-iniciar")
    public Map<String,Object> puedeIniciar(@RequestParam("idOrden") Long idOrden) {
        long activos = valeRepo.countByOrdenProduccion_IdAndEstado(idOrden, EstadoVale.EN_PROCESO);
        var vales = valeRepo.findByOrdenProduccion_Id(idOrden);
        return Map.of(
                "idOrden", idOrden,
                "activosEN_PROCESO", activos,
                "vales", vales.stream().map(v -> Map.of(
                        "id", v.getId(),
                        "codigo", v.getCodigoVale(),
                        "estado", String.valueOf(v.getEstado()),
                        "puestoActual", v.getPuestoActual()
                )).toList()
        );
    }

}

