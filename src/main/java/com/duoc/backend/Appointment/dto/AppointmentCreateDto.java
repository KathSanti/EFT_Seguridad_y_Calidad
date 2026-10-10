package com.duoc.backend.Appointment.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


public record AppointmentCreateDto(
    @NotNull(message = "La fecha no puede estar vacía")
    LocalDate date,

    @NotNull(message = "La fecha no puede estar vacía")
    LocalTime time,

    @NotBlank(message = "Debe especificar una razón de consulta")
    String reason,

    @NotBlank(message = "Debe escoger un especialista")
    String veterinarian

   
    
) {}