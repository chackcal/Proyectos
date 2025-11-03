package com.santi.linea.repositories;

import com.santi.linea.models.Formula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FormulaRepository extends JpaRepository<Formula, Long> {
    List<Formula> findByProductoFinal_Id(Long productoId);

}
