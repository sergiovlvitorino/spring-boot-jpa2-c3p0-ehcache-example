package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service.AuthService;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.controller.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

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
class SecurityHeadersTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void response_shouldContainXContentTypeOptionsHeader() throws Exception {
        mockMvc.perform(get("/api/person/test"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void response_shouldContainXFrameOptionsHeader() throws Exception {
        mockMvc.perform(get("/api/person/test"))
                .andExpect(header().string("X-Frame-Options", "DENY"));
    }

    @Test
    void response_shouldContainCacheControlHeader() throws Exception {
        mockMvc.perform(get("/api/person/test"))
                .andExpect(header().exists("Cache-Control"));
    }
}
