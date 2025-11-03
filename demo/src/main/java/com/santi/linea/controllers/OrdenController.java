package com.santi.linea.controllers;

import com.santi.linea.models.OrdenProduccion;
import com.santi.linea.services.OrdenService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
@CrossOrigin
public class OrdenController {
    private final OrdenService ordenService;
    public OrdenController(OrdenService s) { this.ordenService = s; }

    // Crear OP
    @PostMapping
    public OrdenProduccion crear(@RequestParam Long productoId,
                                 @RequestParam Long formulaId,
                                 @RequestParam int cantidad) {
        return ordenService.crearOP(productoId, formulaId, cantidad);
    }

    @GetMapping("/activas")
    public List<OrdenProduccion> activas() { return ordenService.activas(); }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenProduccion> getById(@PathVariable Long id) {
        return ordenService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
