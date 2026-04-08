package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;

@Component
public class SecurityPropertiesValidator {

    private static final Logger log = LoggerFactory.getLogger(SecurityPropertiesValidator.class);
    private static final int MIN_JWT_SECRET_BYTES = 32;

    static final String DEFAULT_JWT_SECRET = "dev-only-insecure-key-change-me-in-production!";
    static final String DEFAULT_ADMIN_PASSWORD = "changeme";
    static final Set<String> DEV_PROFILES = Set.of("default", "dev", "test");

    private final Environment environment;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.admin.password}")
    private String adminPassword;

    public SecurityPropertiesValidator(Environment environment) {
        this.environment = environment;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void validate() {
        if (isProductionProfile()) {
            validateJwtSecretLength();
            if (DEFAULT_JWT_SECRET.equals(jwtSecret)) {
                throw new IllegalStateException(
                        "Default JWT secret detected in production! Set APP_JWT_SECRET environment variable.");
            }
            if (DEFAULT_ADMIN_PASSWORD.equals(adminPassword)) {
                throw new IllegalStateException(
                        "Default admin password detected in production! Set APP_ADMIN_PASSWORD environment variable.");
            }
        } else {
            if (DEFAULT_JWT_SECRET.equals(jwtSecret)) {
                log.warn("Using default JWT secret — acceptable only for development/testing.");
            }
            if (DEFAULT_ADMIN_PASSWORD.equals(adminPassword)) {
                log.warn("Using default admin password — acceptable only for development/testing.");
            }
        }
    }

    private void validateJwtSecretLength() {
        int secretBytes = jwtSecret.getBytes(StandardCharsets.UTF_8).length;
        if (secretBytes < MIN_JWT_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT secret must be at least " + MIN_JWT_SECRET_BYTES + " bytes (256 bits) for HS256. " +
                    "Current length: " + secretBytes + " bytes.");
        }
    }

    boolean isProductionProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) return false;
        return Arrays.stream(activeProfiles).noneMatch(DEV_PROFILES::contains);
    }
}
