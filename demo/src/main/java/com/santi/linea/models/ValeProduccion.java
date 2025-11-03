package com.santi.linea.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ValeProduccion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_produccion_id")
    @JsonBackReference
    private OrdenProduccion ordenProduccion;

    @Column(unique = true, nullable = false)
    private String codigoVale;
    @Enumerated(EnumType.STRING)
    private EstadoVale estado;

    @Column(nullable = false)
    private Integer puestoActual; // 1..N
    private LocalDateTime creadoEn;
    private LocalDateTime finalizadoEn;
}