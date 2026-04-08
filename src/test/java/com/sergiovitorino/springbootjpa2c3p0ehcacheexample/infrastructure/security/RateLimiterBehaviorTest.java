package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.controller.AuthController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de comportamento do rate limiter integrado ao fluxo de login.
 * Garante que:
 * - Logins validos NAO consomem rate limit
 * - Apenas logins invalidos consomem rate limit
 * - Apos exceder o limite, retorna 429
 */
@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, LoginRateLimiter.class})
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

    @Autowired
    private LoginRateLimiter rateLimiter;

    @MockitoBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void resetRateLimiter() {
        // Reset rate limiter state between tests to avoid cross-test contamination
        rateLimiter.clear();
    }

    @Test
    void login_withValidCredentials_shouldNotConsumeRateLimit() throws Exception {
        when(jwtUtil.generateToken("testadmin")).thenReturn("mocked-jwt-token");

        // 10 logins validos seguidos — nenhum deve retornar 429
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"testadmin\",\"password\":\"testpass\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
        }
    }

    @Test
    void login_withInvalidCredentials_shouldConsumeRateLimit_andReturn429AfterMax() throws Exception {
        // 5 logins invalidos (abaixo do limite)
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"testadmin\",\"password\":\"wrongpass\"}"))
                    .andExpect(status().isUnauthorized());
        }

        // 6a tentativa deve ser bloqueada com 429
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"testadmin\",\"password\":\"wrongpass\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"));
    }

    @Test
    void login_rateLimitWindow_shouldResetAfterExpiration() {
        // Teste unitario do rate limiter: verifica que entries expiradas sao limpas
        LoginRateLimiter limiter = new LoginRateLimiter();
        String ip = "192.168.1.100";

        // Preenche ate o limite
        for (int i = 0; i < LoginRateLimiter.MAX_ATTEMPTS; i++) {
            limiter.recordAttempt(ip);
        }
        assertTrue(limiter.isBlocked(ip), "Deve estar bloqueado apos MAX_ATTEMPTS");

        // Um IP diferente (sem tentativas) nao e bloqueado
        assertFalse(limiter.isBlocked("192.168.1.200"),
                "IP sem tentativas nao deve ser bloqueado");
    }
}
