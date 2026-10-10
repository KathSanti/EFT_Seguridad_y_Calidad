package com.duoc.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private JWTAuthenticationConfig jwtAuthtenticationConfig;

    @Mock
    private MyUserDetailsService userDetailsService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginController loginController;

    @Test
    void login_validCredentials_returnsToken() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("hashed");
        user.setEnabled(true);
        user.setRole("ROLE_ADMIN");

        when(userDetailsService.loadUserByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("123", "hashed")).thenReturn(true);
        when(jwtAuthtenticationConfig.getJWTToken("admin", List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .thenReturn("Bearer token123");

        String result = loginController.login("admin", "123");

        assertThat(result).isEqualTo("Bearer token123");
    }

    @Test
    void login_wrongPassword_throwsBadCredentialsException() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("hashed");
        user.setEnabled(true);
        user.setRole("ROLE_ADMIN");

        when(userDetailsService.loadUserByUsername("admin")).thenReturn(user);
        when(passwordEncoder.matches("mala-clave", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> loginController.login("admin", "mala-clave"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void login_disabledAccount_throwsDisabledException() {
        User user = new User();
        user.setUsername("admin");
        user.setPassword("hashed");
        user.setEnabled(false);
        user.setRole("ROLE_ADMIN");

        when(userDetailsService.loadUserByUsername("admin")).thenReturn(user);

        assertThatThrownBy(() -> loginController.login("admin", "123"))
                .isInstanceOf(DisabledException.class);
    }
}
