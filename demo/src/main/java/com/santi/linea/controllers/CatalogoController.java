package com.santi.linea.controllers;

import com.santi.linea.dto.OpcionDTO;
import com.santi.linea.repositories.ProductoRepository;
import com.santi.linea.repositories.FormulaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/catalogo")
@RequiredArgsConstructor
public class CatalogoController {

    private final ProductoRepository productoRepo;
    private final FormulaRepository formulaRepo;

    @GetMapping("/productos")
    public List<OpcionDTO> productos() {
        return productoRepo.findAll().stream()
                .map(p -> new OpcionDTO(p.getId(), p.getNombre()))
                .toList();
    }

    @GetMapping("/formulas")
    public List<OpcionDTO> formulas(@RequestParam Long productoId) {
        return formulaRepo.findByProductoFinal_Id(productoId).stream()
                .map(f -> new OpcionDTO(f.getId(), "Fórmula " + f.getId()))
                .toList();
    }
}
