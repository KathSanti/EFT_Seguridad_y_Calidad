package com.duoc.backend.user;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.duoc.backend.UserRepository;
import com.duoc.backend.user.dto.UserCreateDto;

@RestController
@RequestMapping("/usuarios")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
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
}