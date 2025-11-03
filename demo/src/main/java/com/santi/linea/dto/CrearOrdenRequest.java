package com.santi.linea.dto;

import lombok.Data;

@Data
public class CrearOrdenRequest {
    private String codigoOrden;
    private Long productoFinalId;
    private Long formulaId;
    private Integer cantidad;
    private String fechaInicio;
}
