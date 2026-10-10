package com.duoc.backend.Care.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CareCreateDto (

    @NotBlank(message = "Debe especificar un nombre")
    String name,
    
    @NotNull(message = "El costo es obligatorio")
    @Min(value = 0, message = "El costo no puede ser negativo")
    Double cost
    
){}
