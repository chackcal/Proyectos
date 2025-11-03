package com.santi.linea.repositories;

import com.santi.linea.models.Producto;
import com.santi.linea.models.Tipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByTipoAndCodigo(Tipo tipo, String codigo);
    Optional<Producto> findByCodigo(String codigo);
}
