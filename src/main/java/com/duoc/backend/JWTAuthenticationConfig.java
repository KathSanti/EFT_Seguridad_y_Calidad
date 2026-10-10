package com.duoc.backend;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;

import static com.duoc.backend.Constants.getSigningKey;

import io.jsonwebtoken.Jwts;

@Configuration
public class JWTAuthenticationConfig {

    private final String jwtSecret;

    public JWTAuthenticationConfig(@Value("${jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    // recibe Los roles de la base de datos
    public String getJWTToken(String username, Collection<? extends GrantedAuthority> authorities) {

        Map<String, Object> claims = new HashMap<>();
        
        // Usamos los roles reales que llegaron por parámetro
        claims.put("authorities", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        //Calculamos el tiempo con api recomendadas 
               
        Instant now = Instant.now();
        Instant expiration = now.plus(1440, ChronoUnit.MINUTES); // 24hrs 

        String token = Jwts.builder()
                .claims()
                .add(claims)
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .and()
                .signWith(getSigningKey(jwtSecret))
                .compact();

        return "Bearer " + token;
    }
}