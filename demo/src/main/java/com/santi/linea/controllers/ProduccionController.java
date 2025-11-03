package com.santi.linea.controllers;

import com.santi.linea.models.ValeProduccion;
import com.santi.linea.services.ProduccionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/produccion")
@CrossOrigin
public class ProduccionController {
    private final ProduccionService service;
    public ProduccionController(ProduccionService s){ this.service = s; }

    // Iniciar vale
    @PostMapping("/vales/iniciar")
    public ValeProduccion iniciarVale(@RequestParam Long opId) {
        return service.iniciarVale(opId);
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

}

