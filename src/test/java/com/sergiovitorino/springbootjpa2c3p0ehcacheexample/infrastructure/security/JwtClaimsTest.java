package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes dos claims JWT para garantir que tokens gerados contem
 * todas as informacoes obrigatorias (issuer, audience, subject, exp, iat).
 */
class JwtClaimsTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-chars-long!";
    private static final String USERNAME = "testuser";

    private JwtUtil jwtUtil;
    private JwtDecoder decoder;

    @BeforeEach
    void setUp() {
        SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        jwtUtil = new JwtUtil(encoder, decoder, 60L);
    }

    @Test
    void generateToken_shouldContainIssuerClaim() {
        String token = jwtUtil.generateToken(USERNAME);
        Jwt jwt = decoder.decode(token);

        assertEquals("spring-boot-jpa2-app", jwt.getClaim("iss"),
                "Token deve conter issuer 'spring-boot-jpa2-app'");
    }

    @Test
    void generateToken_shouldContainAudienceClaim() {
        String token = jwtUtil.generateToken(USERNAME);
        Jwt jwt = decoder.decode(token);

        assertNotNull(jwt.getAudience(), "Token deve conter claim audience");
        assertTrue(jwt.getAudience().contains("spring-boot-jpa2-app"),
                "Audience deve conter 'spring-boot-jpa2-app'");
    }

    @Test
    void generateToken_shouldContainSubjectClaim() {
        String token = jwtUtil.generateToken(USERNAME);
        Jwt jwt = decoder.decode(token);

        assertEquals(USERNAME, jwt.getSubject(),
                "Token deve conter subject com o username");
    }

    @Test
    void generateToken_shouldContainExpirationClaim() {
        String token = jwtUtil.generateToken(USERNAME);
        Jwt jwt = decoder.decode(token);

        assertNotNull(jwt.getExpiresAt(),
                "Token deve conter claim 'exp' (expiration)");
    }

    @Test
    void generateToken_shouldContainIssuedAtClaim() {
        String token = jwtUtil.generateToken(USERNAME);
        Jwt jwt = decoder.decode(token);

        assertNotNull(jwt.getIssuedAt(),
                "Token deve conter claim 'iat' (issued at)");
    }
}
