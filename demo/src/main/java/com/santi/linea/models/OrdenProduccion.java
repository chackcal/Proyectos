package com.santi.linea.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrdenProduccion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codigoOrden;

    @OneToMany(mappedBy = "ordenProduccion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<ValeProduccion> vales;

    @ManyToOne
    @JoinColumn(name = "producto_final_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "formulas", "ordenes"})
    private Producto productoFinal;

    @ManyToOne
    @JoinColumn(name = "formula_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "detalles", "puestos"})
    private Formula formula;

    @Column(nullable = false)
    private int cantidad;
    @Column(nullable = false)
    private int producidas;

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    @Enumerated(EnumType.STRING) private EstadoOrden estado;


}