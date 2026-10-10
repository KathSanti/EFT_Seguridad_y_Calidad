package com.duoc.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MyUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MyUserDetailsService myUserDetailsService;

    @Test
    void loadUserByUsername_found_returnsUser() {
        User user = new User();
        user.setUsername("admin");
        user.setRole("ROLE_ADMIN");
        user.setEnabled(true);
        when(userRepository.findByUsername("admin")).thenReturn(user);

        UserDetails result = myUserDetailsService.loadUserByUsername("admin");

        assertThat(result.getUsername()).isEqualTo("admin");
    }

    @Test
    void loadUserByUsername_notFound_throwsUsernameNotFoundException() {
        when(userRepository.findByUsername("ghost")).thenReturn(null);

        assertThatThrownBy(() -> myUserDetailsService.loadUserByUsername("ghost"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void passwordEncoder_isBCryptAndVerifiesCorrectly() {
        PasswordEncoder encoder = myUserDetailsService.passwordEncoder();

        String hash = encoder.encode("123");

        assertThat(hash).isNotEqualTo("123");
        assertThat(encoder.matches("123", hash)).isTrue();
        assertThat(encoder.matches("clave-incorrecta", hash)).isFalse();
    }
}
