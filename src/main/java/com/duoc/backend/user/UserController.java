package com.duoc.backend.user;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.backend.User;
import com.duoc.backend.UserRepository;
import com.duoc.backend.user.dto.RegisterUserDto;
import com.duoc.backend.user.dto.UserCreateDto;

import jakarta.validation.Valid;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/usuarios")
    public List<UserCreateDto> getAllUsers() {
        // Obtenemos los usuarios reales de MySQL y los transformamos al DTO seguro
        return StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .map(user -> new UserCreateDto(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getRole(),
                        user.isEnabled()
                ))
                .collect(Collectors.toList());
    }

    @PostMapping("/register")
    public UserCreateDto register(@Valid @RequestBody RegisterUserDto dto) {
        if (userRepository.findByUsername(dto.username()) != null) {
            throw new IllegalArgumentException("El nombre de usuario ya existe: " + dto.username());
        }

        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setRole(dto.role());
        user.setEnabled(true);

        User saved = userRepository.save(user);

        return new UserCreateDto(
                saved.getId(),
                saved.getUsername(),
                saved.getEmail(),
                saved.getRole(),
                saved.isEnabled()
        );
    }
}