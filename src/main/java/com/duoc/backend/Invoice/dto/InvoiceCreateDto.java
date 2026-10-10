package com.duoc.backend.Invoice.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List; 

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InvoiceCreateDto (
    
    @NotBlank(message = "Debe especificar un nombre")
    String patientName,

    @NotNull(message = "Debe ingresar una fecha")
    LocalDate date,
    
    @NotNull(message = "Debe ingresar una hora")
    LocalTime time,

    // Nueva función: Recibir Ids de los servicios y medicamentos
    List<Long> careIds,
    List<Long> medicationIds
){}