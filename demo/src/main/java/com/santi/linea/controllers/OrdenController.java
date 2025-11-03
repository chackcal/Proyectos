package com.santi.linea.controllers;

import com.santi.linea.dto.CrearOrdenRequest;
import com.santi.linea.models.OrdenProduccion;
import com.santi.linea.services.OrdenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ordenes")
@CrossOrigin
public class OrdenController {
    private final OrdenService ordenService;
    public OrdenController(OrdenService s) { this.ordenService = s; }

    @GetMapping("/activas")
    public ResponseEntity<List<OrdenProduccion>> activas() {
        var list = ordenService.activas(); // findByEstado(EN_PROCESO)
        return ResponseEntity.ok()
                .header("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0")
                .body(list);
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody CrearOrdenRequest req) {
        try {
            var op = ordenService.crearOrden(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(op);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/finalizadas")
    public List<OrdenProduccion> finalizadas() {
        return ordenService.finalizadas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenProduccion> getById(@PathVariable Long id) {
        return ordenService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
