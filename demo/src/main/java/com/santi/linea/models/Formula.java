package com.santi.linea.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.util.*;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Formula {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Producto productoFinal;

    @OneToMany(mappedBy = "formula", cascade = CascadeType.ALL)
    @JsonIgnoreProperties("formula")
    private List<FormulaDetalle> detalles;

    @OneToMany(mappedBy = "formula")
    @JsonIgnoreProperties("formula")
    private List<FormulaPuestoDetalle> puestos;
}
