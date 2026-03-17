package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleTooManyRequests_shouldReturn429WithMessage() {
        TooManyRequestsException ex = new TooManyRequestsException("Too many login attempts. Please try again later.");
        ResponseEntity<Map<String, Object>> response = handler.handleTooManyRequests(ex);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals(429, response.getBody().get("status"));
        assertEquals("Too Many Requests", response.getBody().get("error"));
        assertEquals("Too many login attempts. Please try again later.", response.getBody().get("message"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    void handleNotFound_shouldReturn404() {
        EntityNotFoundException ex = new EntityNotFoundException("Person not found");
        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().get("status"));
    }

    @Test
    void handleGeneral_shouldReturn500WithGenericMessage() {
        RuntimeException ex = new RuntimeException("Something sensitive leaked");
        ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().get("message"));
        assertFalse(response.getBody().get("message").toString().contains("sensitive"));
    }
}
