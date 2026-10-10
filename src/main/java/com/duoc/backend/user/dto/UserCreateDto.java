package com.duoc.backend.user.dto;


public record UserCreateDto(
    Integer id,
    String username,
    String email,
    String role,
    boolean enabled
    
) {}