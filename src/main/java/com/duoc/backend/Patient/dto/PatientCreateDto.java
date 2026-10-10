package com.duoc.backend.Patient.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PatientCreateDto(
    @NotBlank(message = "El nombre no puede estar vacío")
    String name,

    @NotBlank(message = "La especie es obligatoria")
    String species,

    String breed, // Este campo es opcional, por lo que no lleva etiqueta

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 0, message = "La edad no puede ser negativa")
    Integer age,

    @NotBlank(message = "El nombre del dueño es obligatorio")
    String owner
) {}