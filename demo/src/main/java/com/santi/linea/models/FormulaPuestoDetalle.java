package com.santi.linea.models;

import jakarta.persistence.*;

import lombok.*;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"formula_id","puesto_id","componente_id"}))
public class FormulaPuestoDetalle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Formula formula;
    @ManyToOne(optional = false)
    private Puesto puesto;

    @ManyToOne(optional = false)
    private Producto componente; // debe ser COMPONENTE
    @Column(nullable = false)
    private int cantidad;
}
