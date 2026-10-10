package com.duoc.backend.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.duoc.backend.User;
import com.duoc.backend.UserRepository;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private User buildUser(Integer id, String username, String role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(username + "@veterinaria.cl");
        user.setRole(role);
        user.setEnabled(true);
        return user;
    }

    @Test
    void getAllUsers_returns200WithMappedDtos() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of(buildUser(1, "admin", "ROLE_ADMIN")));

        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"))
                .andExpect(jsonPath("$[0].role").value("ROLE_ADMIN"));
    }

    @Test
    void register_validBody_returns200AndCreatesUser() throws Exception {
        when(userRepository.findByUsername("nuevo_asistente")).thenReturn(null);
        when(passwordEncoder.encode("123")).thenReturn("hashed-123");
        when(userRepository.save(any(User.class))).thenReturn(buildUser(5, "nuevo_asistente", "ROLE_ASISTENTE"));

        String body = """
                {
                  "username": "nuevo_asistente",
                  "email": "nuevo@veterinaria.cl",
                  "password": "123",
                  "role": "ROLE_ASISTENTE"
                }
                """;

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("nuevo_asistente"))
                .andExpect(jsonPath("$.role").value("ROLE_ASISTENTE"));
    }

    @Test
    void register_usernameAlreadyExists_returns400() throws Exception {
        when(userRepository.findByUsername("admin")).thenReturn(buildUser(1, "admin", "ROLE_ADMIN"));

        String body = """
                {
                  "username": "admin",
                  "email": "otro@veterinaria.cl",
                  "password": "123",
                  "role": "ROLE_ASISTENTE"
                }
                """;

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidRole_returns400() throws Exception {
        String body = """
                {
                  "username": "nuevo",
                  "email": "nuevo@veterinaria.cl",
                  "password": "123",
                  "role": "ROLE_NO_EXISTE"
                }
                """;

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_blankUsername_returns400() throws Exception {
        String body = """
                {
                  "username": "",
                  "email": "nuevo@veterinaria.cl",
                  "password": "123",
                  "role": "ROLE_ASISTENTE"
                }
                """;

        mockMvc.perform(post("/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }
}
