package com.duoc.backend;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private final JWTAuthenticationConfig jwtAuthtenticationConfig;
    private final MyUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    
    public LoginController(
            JWTAuthenticationConfig jwtAuthtenticationConfig,
            MyUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        this.jwtAuthtenticationConfig = jwtAuthtenticationConfig;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("login")
    public String login(
            @RequestParam("user") String username,
            @RequestParam("encryptedPass") String encryptedPass) {

        final UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (!userDetails.isEnabled()) {
            throw new DisabledException("Cuenta de usuario deshabilitada");
        }

        if (!passwordEncoder.matches(encryptedPass, userDetails.getPassword())) {
           throw new BadCredentialsException("Credenciales inválidas");
        }

        return jwtAuthtenticationConfig.getJWTToken(username, userDetails.getAuthorities());
    }
}