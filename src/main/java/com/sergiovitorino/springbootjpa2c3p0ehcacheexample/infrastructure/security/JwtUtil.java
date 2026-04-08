package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);
    private static final String ISSUER = "spring-boot-jpa2-app";
    private static final List<String> AUDIENCE = List.of("spring-boot-jpa2-app");

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final long expirationMinutes;

    public JwtUtil(JwtEncoder encoder,
                   JwtDecoder decoder,
                   @Value("${app.jwt.expiration-minutes:60}") long expirationMinutes) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.expirationMinutes = expirationMinutes;
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(username)
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .issuedAt(now)
                .expiresAt(now.plus(expirationMinutes, ChronoUnit.MINUTES))
                .build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public String validateAndGetSubject(String token) {
        try {
            Jwt jwt = decoder.decode(token);
            return jwt.getSubject();
        } catch (JwtException e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return null;
        }
    }

}
