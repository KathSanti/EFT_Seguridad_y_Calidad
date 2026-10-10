package com.duoc.backend;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity()
@EnableMethodSecurity
@Configuration
@Profile("default")
class WebSecurityConfig{

    
    private final JWTAuthorizationFilter jwtAuthorizationFilter;

    public WebSecurityConfig(JWTAuthorizationFilter jwtAuthorizationFilter) {
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                // Forzar que la API sea sin estado ignora las cookies JSESSIONID
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                .authorizeHttpRequests( authz -> authz
                        
                        .requestMatchers(HttpMethod.POST, Constants.LOGIN_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, Constants.LOGIN_URL).permitAll()
                        .requestMatchers(HttpMethod.GET, "/usuarios").hasAuthority(Constants.ROL_ADMIN)
                        .requestMatchers(HttpMethod.POST, "/register").hasAuthority(Constants.ROL_ADMIN)

                        // Pacientes solo Lectura para cualquier usuario autenticado
                        .requestMatchers(HttpMethod.GET, "/patient/**").authenticated()
                        // Solo Admin y Asistente pueden modificar, crear o borrar citas en cualquier ruta /patient/**
                        .requestMatchers("/patient/**").hasAnyAuthority(Constants.ROL_ADMIN, Constants.ROL_ASISTENTE)

                        // Citas solo lectura para cualquier usuario autenticado
                        .requestMatchers(HttpMethod.GET, "/appointment/**").authenticated()
                        // Solo Admin y Asistente pueden modificar, crear o borrar citas en cualquier ruta /appointment/**
                        .requestMatchers("/appointment/**").hasAnyAuthority(Constants.ROL_ADMIN, Constants.ROL_ASISTENTE)

                        .requestMatchers(HttpMethod.GET, "/medication/**").authenticated()
                        .requestMatchers("/medication/**").hasAnyAuthority(Constants.ROL_ADMIN, Constants.ROL_DOCTOR)

                        .requestMatchers(HttpMethod.GET, "/care/**").authenticated()
                        .requestMatchers("/care/**").hasAnyAuthority(Constants.ROL_ADMIN, Constants.ROL_ASISTENTE)

                        .requestMatchers(HttpMethod.GET, "/invoice/**").authenticated()
                        .requestMatchers("/invoice/**").hasAnyAuthority(Constants.ROL_ADMIN, Constants.ROL_ASISTENTE)

                        .anyRequest().authenticated())
                .addFilterAfter(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class);
                
        return http.build();
    }
}