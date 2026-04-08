package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testes de validacao do JWT secret para garantir que:
 * - Secrets curtos (< 32 bytes) sao rejeitados em producao
 * - Secrets com tamanho adequado sao aceitos
 * - O secret default do projeto e rejeitado em producao
 */
class JwtSecretValidationTest {

    private SecurityPropertiesValidator createValidator(String jwtSecret, String adminPassword, String... profiles) {
        Environment env = mock(Environment.class);
        when(env.getActiveProfiles()).thenReturn(profiles);

        SecurityPropertiesValidator validator = new SecurityPropertiesValidator(env);
        ReflectionTestUtils.setField(validator, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(validator, "adminPassword", adminPassword);
        return validator;
    }

    @Test
    void validate_withShortSecret_inProduction_shouldThrowIllegalStateException() {
        // Secret com menos de 32 bytes deve ser rejeitado em prod
        SecurityPropertiesValidator validator = createValidator(
                "short-secret", "secure-password-123", "prod");

        IllegalStateException ex = assertThrows(IllegalStateException.class, validator::validate);
        assertTrue(ex.getMessage().toLowerCase().contains("jwt secret") ||
                        ex.getMessage().toLowerCase().contains("jwt") ||
                        ex.getMessage().toLowerCase().contains("secret"),
                "Mensagem deve indicar problema com JWT secret: " + ex.getMessage());
    }

    @Test
    void validate_withSecretOf32PlusBytes_inProduction_shouldNotThrow() {
        // Secret com 32+ bytes deve ser aceito em prod
        String validSecret = "a-production-secret-that-is-at-least-32-bytes!!";
        assertTrue(validSecret.getBytes().length >= 32, "Pre-condicao: secret deve ter 32+ bytes");

        SecurityPropertiesValidator validator = createValidator(
                validSecret, "secure-password-123", "prod");

        assertDoesNotThrow(validator::validate);
    }

    @Test
    void validate_withDefaultSecret_inProduction_shouldThrowIllegalStateException() {
        // O secret default do projeto deve ser rejeitado em prod
        SecurityPropertiesValidator validator = createValidator(
                SecurityPropertiesValidator.DEFAULT_JWT_SECRET, "secure-password-123", "prod");

        IllegalStateException ex = assertThrows(IllegalStateException.class, validator::validate);
        assertTrue(ex.getMessage().contains("JWT secret") || ex.getMessage().contains("JWT"),
                "Mensagem deve indicar problema com JWT secret");
    }

    @Test
    void validate_withShortSecret_inDevProfile_shouldNotThrow() {
        // Em dev, secrets curtos sao aceitos (apenas warning)
        SecurityPropertiesValidator validator = createValidator(
                "short", SecurityPropertiesValidator.DEFAULT_ADMIN_PASSWORD, "dev");

        assertDoesNotThrow(validator::validate);
    }

    @Test
    void validate_withExactly32ByteSecret_inProduction_shouldNotThrow() {
        // Boundary test: exatamente 32 bytes
        String exact32 = "abcdefghijklmnopqrstuvwxyz123456"; // 32 chars = 32 bytes (ASCII)
        assertEquals(32, exact32.getBytes().length, "Pre-condicao: secret deve ter exatamente 32 bytes");

        SecurityPropertiesValidator validator = createValidator(
                exact32, "secure-password-123", "prod");

        assertDoesNotThrow(validator::validate);
    }
}
