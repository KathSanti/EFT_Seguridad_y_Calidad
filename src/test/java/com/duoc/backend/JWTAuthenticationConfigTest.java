package com.duoc.backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

class JWTAuthenticationConfigTest {

    private static final String TEST_SECRET = "una-clave-de-prueba-suficientemente-larga-para-hs512-1234567890";

    private final JWTAuthenticationConfig jwtAuthenticationConfig = new JWTAuthenticationConfig(TEST_SECRET);

    @Test
    void getJWTToken_returnsBearerTokenWithRolesAndSubject() {
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

        String token = jwtAuthenticationConfig.getJWTToken("admin", authorities);

        assertThat(token).startsWith("Bearer ");

        SecretKey key = (SecretKey) Constants.getSigningKey(TEST_SECRET);
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token.replace("Bearer ", ""))
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo("admin");
        @SuppressWarnings("unchecked")
        List<String> authoritiesClaim = (List<String>) claims.get("authorities");
        assertThat(authoritiesClaim).containsExactly("ROLE_ADMIN");
    }
}
