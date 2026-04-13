package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtExpirationTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-chars-long!";

    private JwtEncoder createEncoder() {
        SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return new NimbusJwtEncoder(new ImmutableSecret<>(key));
    }

    private JwtDecoder createDecoder() {
        SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }

    @Test
    void validateAndGetSubject_withExpiredToken_shouldReturnNull() {
        // Generate a token that is already expired (issuedAt in the past, expiresAt also in the past)
        JwtEncoder encoder = createEncoder();
        JwtDecoder decoder = createDecoder();

        Instant pastIssue = Instant.now().minus(10, ChronoUnit.MINUTES);
        Instant pastExpiry = Instant.now().minus(5, ChronoUnit.MINUTES);

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject("testuser")
                .issuer("spring-boot-jpa2-app")
                .audience(List.of("spring-boot-jpa2-app"))
                .issuedAt(pastIssue)
                .expiresAt(pastExpiry)
                .build();
        String expiredToken = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        // Create JwtUtil just for validation
        JwtUtil jwtUtil = new JwtUtil(encoder, decoder, 60L);
        String subject = jwtUtil.validateAndGetSubject(expiredToken);

        assertNull(subject, "Expired token should return null");
    }

    @Test
    void validateAndGetSubject_withValidToken_shouldReturnSubject() {
        JwtEncoder encoder = createEncoder();
        JwtDecoder decoder = createDecoder();
        JwtUtil jwtUtil = new JwtUtil(encoder, decoder, 60L);

        String token = jwtUtil.generateToken("admin");
        String subject = jwtUtil.validateAndGetSubject(token);

        assertNotNull(subject);
        assertEquals("admin", subject);
    }
}
