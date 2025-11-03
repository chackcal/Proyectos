package com.santi.linea.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"tipo", "codigo"}))
public class Producto {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String codigo;
    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private Tipo tipo;

    @Column(nullable = false) private int stock;

    private LocalDate fechaFinalizacion;

    @OneToMany(mappedBy = "productoFinal")
    @JsonIgnore
    private List<OrdenProduccion> ordenes;

    @OneToMany(mappedBy = "componente")
    @JsonIgnore
    private List<FormulaDetalle> formulaDetalles;
}

