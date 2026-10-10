package com.duoc.backend.Medication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record MedicationCreateDto(
    @NotBlank(message = "El nombre del medicamento es obligatorio")
    String name,

    @NotNull(message = "El costo es obligatorio")
    @PositiveOrZero(message = "El costo no puede ser un valor negativo")
    Double cost
) {}