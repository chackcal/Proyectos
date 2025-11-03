package com.santi.linea.controllers;

import com.santi.linea.models.EstadoOrden;
import com.santi.linea.models.OrdenProduccion;
import com.santi.linea.repositories.OrdenProduccionRepository;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin
public class DashboardController {

    private final OrdenProduccionRepository repo;
    public DashboardController(OrdenProduccionRepository r){ this.repo = r; }

    @GetMapping("/resumen")
    public Map<String,Object> resumen(){
        List<OrdenProduccion> activas = repo.findByEstado(EstadoOrden.EN_PROCESO);
        int totalPlan = activas.stream().mapToInt(OrdenProduccion::getCantidad).sum();
        int totalProd = activas.stream().mapToInt(OrdenProduccion::getProducidas).sum();

        Map<String,Object> out = new HashMap<>();
        out.put("opActivas", activas.size());
        out.put("unidadesPlanificadas", totalPlan);
        out.put("unidadesProducidas", totalProd);
        out.put("avanceGlobal", totalPlan == 0 ? 0 : Math.round(100.0 * totalProd / totalPlan));
        return out;
    }
}
