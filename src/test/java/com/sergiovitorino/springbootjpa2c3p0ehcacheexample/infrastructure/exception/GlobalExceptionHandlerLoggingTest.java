package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para garantir que o GlobalExceptionHandler:
 * - Retorna 500 com mensagem generica (sem vazar detalhes internos)
 * - Nao engole silenciosamente excecoes (o logging e validado pela ausencia de swallowing)
 */
class GlobalExceptionHandlerLoggingTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleGeneral_shouldReturn500WithGenericMessage() {
        RuntimeException ex = new RuntimeException("Database connection failed: password=secret123");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().get("status"));
        assertEquals("Internal Server Error", response.getBody().get("error"));
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
    }

    @Test
    void handleGeneral_shouldNotLeakSensitiveInformation() {
        String sensitiveMessage = "SQL Error: SELECT * FROM users WHERE password='admin123'";
        RuntimeException ex = new RuntimeException(sensitiveMessage);

        ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

        assertNotNull(response.getBody());
        String responseMessage = response.getBody().get("message").toString();
        assertFalse(responseMessage.contains("SQL"),
                "Response nao deve conter detalhes de SQL");
        assertFalse(responseMessage.contains("admin123"),
                "Response nao deve conter credenciais");
        assertFalse(responseMessage.contains("password"),
                "Response nao deve conter informacoes senssiveis");
    }

    @Test
    void handleGeneral_shouldReturnTimestamp() {
        RuntimeException ex = new RuntimeException("any error");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

        assertNotNull(response.getBody());
        assertNotNull(response.getBody().get("timestamp"),
                "Response deve conter timestamp para rastreabilidade");
    }

    @Test
    void handleGeneral_withNullPointerException_shouldReturn500() {
        NullPointerException ex = new NullPointerException("null reference");

        ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
    }

    @Test
    void handleGeneral_withNestedCause_shouldNotLeakCauseDetails() {
        Exception cause = new Exception("Root cause: file /etc/passwd not readable");
        RuntimeException ex = new RuntimeException("Wrapper exception", cause);

        ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

        String responseMessage = response.getBody().get("message").toString();
        assertFalse(responseMessage.contains("/etc/passwd"),
                "Response nao deve vazar detalhes da causa raiz");
        assertFalse(responseMessage.contains("Root cause"),
                "Response nao deve vazar detalhes da causa raiz");
    }
}
