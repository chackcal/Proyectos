package com.santi.linea.repositories;

import com.santi.linea.models.ValePuestoRequisito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ValePuestoRequisitoRepository extends JpaRepository<ValePuestoRequisito, Long> {
    List<ValePuestoRequisito> findByVale_IdAndPuesto_Id(Long valeId, Long puestoId);
    Optional<ValePuestoRequisito> findByVale_IdAndPuesto_IdAndComponente_Id(Long valeId, Long puestoId, Long componenteId);

    // Incremento atómico (evita sobre-escaneo)
    @Modifying
    @Query("update ValePuestoRequisito r set r.escaneado = r.escaneado + 1 " +
            "where r.id = :id and r.escaneado < r.requerido")
    int tryIncrement(@Param("id") Long id);

    // Decremento seguro para deshacer
    @Modifying
    @Query("update ValePuestoRequisito r set r.escaneado = r.escaneado - 1 " +
            "where r.id = :id and r.escaneado > 0")
    int tryDecrement(@Param("id") Long id);

    @Query("select (count(r) = sum(case when r.escaneado >= r.requerido then 1 else 0 end)) " +
            "from ValePuestoRequisito r where r.vale.id = :valeId and r.puesto.id = :puestoId")
    boolean isPuestoCompleto(@Param("valeId") Long valeId, @Param("puestoId") Long puestoId);

    @Query("""
  select r from ValePuestoRequisito r
  where r.vale.codigoVale = :codigoVale
    and r.puesto.ordenSecuencia = :puestoSecuencia
  order by r.componente.nombre asc
""")
    List<ValePuestoRequisito> checklist(@Param("codigoVale") String codigoVale,
                                        @Param("puestoSecuencia") int puestoSecuencia);
}
