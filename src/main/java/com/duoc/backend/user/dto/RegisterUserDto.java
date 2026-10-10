package com.duoc.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterUserDto(
    @NotBlank(message = "El nombre de usuario es obligatorio")
    String username,

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 3, message = "La contraseña debe tener al menos 3 caracteres")
    String password,

    @NotBlank(message = "El rol es obligatorio")
    @Pattern(
        regexp = "ROLE_ADMIN|ROLE_ASISTENTE|ROLE_DOCTOR|ROLE_USER",
        message = "Rol inválido. Debe ser ROLE_ADMIN, ROLE_ASISTENTE, ROLE_DOCTOR o ROLE_USER"
    )
    String role
) {}
