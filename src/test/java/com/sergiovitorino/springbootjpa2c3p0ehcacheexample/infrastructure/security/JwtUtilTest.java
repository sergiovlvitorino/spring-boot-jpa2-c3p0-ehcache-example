package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private static final String SECRET = "test-secret-key-must-be-at-least-32-chars-long!";

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        SecretKeySpec key = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();

        jwtUtil = new JwtUtil(encoder, decoder, 60L);
    }

    @Test
    void generateToken_shouldReturnNonBlankToken() {
        String token = jwtUtil.generateToken("testuser");
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void generateToken_shouldEncodeSubject() {
        String token = jwtUtil.generateToken("testuser");
        String subject = jwtUtil.validateAndGetSubject(token);
        assertEquals("testuser", subject);
    }

    @Test
    void generateToken_differentUsers_shouldProduceDifferentTokens() {
        String token1 = jwtUtil.generateToken("user1");
        String token2 = jwtUtil.generateToken("user2");
        assertNotEquals(token1, token2);
    }

    @Test
    void validateAndGetSubject_withValidToken_shouldReturnSubject() {
        String token = jwtUtil.generateToken("admin");
        String subject = jwtUtil.validateAndGetSubject(token);
        assertEquals("admin", subject);
    }

    @Test
    void validateAndGetSubject_withInvalidToken_shouldReturnNull() {
        String subject = jwtUtil.validateAndGetSubject("not.a.valid.token");
        assertNull(subject);
    }

    @Test
    void validateAndGetSubject_withTamperedToken_shouldReturnNull() {
        String token = jwtUtil.generateToken("testuser");
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        String subject = jwtUtil.validateAndGetSubject(tampered);
        assertNull(subject);
    }

    @Test
    void validateAndGetSubject_withEmptyString_shouldReturnNull() {
        String subject = jwtUtil.validateAndGetSubject("");
        assertNull(subject);
    }
}
