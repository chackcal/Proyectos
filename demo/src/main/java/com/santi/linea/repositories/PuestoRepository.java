package com.santi.linea.repositories;

import com.santi.linea.models.Puesto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PuestoRepository extends JpaRepository<Puesto, Long> {
    List<Puesto> findAllByOrderByOrdenSecuenciaAsc();
}
