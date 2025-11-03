package com.santi.linea.models;

import jakarta.persistence.*;
import lombok.*;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FormulaDetalle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Formula formula;
    @ManyToOne(optional = false)
    private Producto componente;
    @Column(nullable = false)
    private int cantidad;
}
