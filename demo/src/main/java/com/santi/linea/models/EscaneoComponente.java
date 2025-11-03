package com.santi.linea.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class EscaneoComponente {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ValeProduccion vale;
    @ManyToOne(optional = false)
    private Puesto puesto;
    @ManyToOne(optional = false)
    private Producto componente;

    @Column(nullable = false)
    private String codigoEscaneado; // lo leído
    private LocalDateTime fechaHora;
}
