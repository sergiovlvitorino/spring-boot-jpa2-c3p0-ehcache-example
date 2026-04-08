package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityPropertiesValidatorTest {

    @Test
    void validate_withDefaultSecretInProduction_shouldThrow() {
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(new String[]{"prod"});

        SecurityPropertiesValidator validator = new SecurityPropertiesValidator(env);
        ReflectionTestUtils.setField(validator, "jwtSecret", SecurityPropertiesValidator.DEFAULT_JWT_SECRET);
        ReflectionTestUtils.setField(validator, "adminPassword", "secure-password-123");

        IllegalStateException ex = assertThrows(IllegalStateException.class, validator::validate);
        assertTrue(ex.getMessage().contains("JWT secret"));
    }

    @Test
    void validate_withDefaultPasswordInProduction_shouldThrow() {
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(new String[]{"prod"});

        SecurityPropertiesValidator validator = new SecurityPropertiesValidator(env);
        ReflectionTestUtils.setField(validator, "jwtSecret", "a-real-production-secret-key-with-32-chars!");
        ReflectionTestUtils.setField(validator, "adminPassword", SecurityPropertiesValidator.DEFAULT_ADMIN_PASSWORD);

        IllegalStateException ex = assertThrows(IllegalStateException.class, validator::validate);
        assertTrue(ex.getMessage().contains("admin password"));
    }

    @Test
    void validate_withSecureValuesInProduction_shouldNotThrow() {
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(new String[]{"prod"});

        SecurityPropertiesValidator validator = new SecurityPropertiesValidator(env);
        ReflectionTestUtils.setField(validator, "jwtSecret", "a-real-production-secret-key-with-32-chars!");
        ReflectionTestUtils.setField(validator, "adminPassword", "secure-password-123");

        assertDoesNotThrow(validator::validate);
    }

    @Test
    void validate_withDefaultValuesInDevProfile_shouldNotThrow() {
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(new String[]{"dev"});

        SecurityPropertiesValidator validator = new SecurityPropertiesValidator(env);
        ReflectionTestUtils.setField(validator, "jwtSecret", SecurityPropertiesValidator.DEFAULT_JWT_SECRET);
        ReflectionTestUtils.setField(validator, "adminPassword", SecurityPropertiesValidator.DEFAULT_ADMIN_PASSWORD);

        assertDoesNotThrow(validator::validate);
    }

    @Test
    void validate_withNoActiveProfiles_shouldNotThrow() {
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(new String[]{});

        SecurityPropertiesValidator validator = new SecurityPropertiesValidator(env);
        ReflectionTestUtils.setField(validator, "jwtSecret", SecurityPropertiesValidator.DEFAULT_JWT_SECRET);
        ReflectionTestUtils.setField(validator, "adminPassword", SecurityPropertiesValidator.DEFAULT_ADMIN_PASSWORD);

        assertDoesNotThrow(validator::validate);
    }

    @Test
    void isProductionProfile_withProdProfile_shouldReturnTrue() {
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(new String[]{"prod"});

        SecurityPropertiesValidator validator = new SecurityPropertiesValidator(env);
        assertTrue(validator.isProductionProfile());
    }

    @Test
    void isProductionProfile_withTestProfile_shouldReturnFalse() {
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(new String[]{"test"});

        SecurityPropertiesValidator validator = new SecurityPropertiesValidator(env);
        assertFalse(validator.isProductionProfile());
    }
}
