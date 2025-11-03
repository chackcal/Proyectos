package com.santi.linea.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"vale_id","puesto_id","componente_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValePuestoRequisito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    private ValeProduccion vale;
    @ManyToOne(optional=false)
    private Puesto puesto;
    @ManyToOne(optional=false)
    private Producto componente;

    @Column(nullable=false) private int requerido;   // cantidad requerida segun Fórmula
    @Column(nullable=false) private int escaneado;   // contador progresivo
}
