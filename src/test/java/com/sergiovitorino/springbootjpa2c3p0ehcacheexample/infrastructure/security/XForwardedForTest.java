package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service.AuthService;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.exception.TooManyRequestsException;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.controller.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.admin.username=admin",
        "app.admin.password=changeme",
        "app.jwt.secret=test-secret-key-must-be-at-least-32-chars!",
        "app.jwt.expiration-minutes=60",
        "app.cors.allowed-origins=http://localhost:3000",
        "spring.cache.type=none"
})
class XForwardedForTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private AuthService authService;

    @Test
    void login_withXForwardedFor_shouldUseFirstIpForRateLimit() throws Exception {
        String json = "{\"username\":\"admin\",\"password\":\"wrongpassword\"}";
        String forwardedIp = "10.0.0.1";
        String xffHeader = "10.0.0.1, 192.168.1.1";

        // Primeiras 5 chamadas: AuthService lanca BadCredentialsException (senha errada)
        when(authService.authenticate(eq("admin"), eq("wrongpassword"), eq(forwardedIp)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Forwarded-For", xffHeader)
                    .content(json));
        }

        // 6a chamada: AuthService lanca TooManyRequestsException (rate limited)
        when(authService.authenticate(eq("admin"), eq("wrongpassword"), eq(forwardedIp)))
                .thenThrow(new TooManyRequestsException("Too many login attempts. Please try again later."));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Forwarded-For", xffHeader)
                        .content(json))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"));
    }
}
