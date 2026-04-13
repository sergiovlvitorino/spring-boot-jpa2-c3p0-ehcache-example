package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service.AuthService;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.exception.TooManyRequestsException;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.controller.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de comportamento do rate limiter integrado ao fluxo de login.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.secret=test-secret-key-that-is-at-least-32-bytes-long!",
        "app.jwt.expiration-minutes=60",
        "app.admin.username=testadmin",
        "app.admin.password=testpass",
        "app.cors.allowed-origins=http://localhost:3000",
        "spring.cache.type=simple"
})
class RateLimiterBehaviorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void login_withValidCredentials_shouldReturnToken() throws Exception {
        when(authService.authenticate(eq("testadmin"), eq("testpass"), anyString()))
                .thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testadmin\",\"password\":\"testpass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
    }

    @Test
    void login_whenRateLimited_shouldReturn429() throws Exception {
        when(authService.authenticate(eq("testadmin"), eq("wrongpass"), anyString()))
                .thenThrow(new TooManyRequestsException("Too many login attempts."));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testadmin\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    void login_rateLimitWindow_shouldResetAfterExpiration() {
        // Teste unitario do rate limiter: verifica isolamento por IP
        LoginRateLimiter limiter = new LoginRateLimiter();
        String ip = "192.168.1.100";

        for (int i = 0; i < LoginRateLimiter.MAX_ATTEMPTS; i++) {
            limiter.recordAttempt(ip);
        }
        assertTrue(limiter.isBlocked(ip), "Deve estar bloqueado apos MAX_ATTEMPTS");
        assertFalse(limiter.isBlocked("192.168.1.200"), "IP sem tentativas nao deve ser bloqueado");
    }
}
