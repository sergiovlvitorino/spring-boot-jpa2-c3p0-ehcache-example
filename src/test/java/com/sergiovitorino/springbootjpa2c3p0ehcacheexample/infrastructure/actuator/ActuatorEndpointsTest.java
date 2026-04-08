package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.actuator;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de integracao para garantir que endpoints do Actuator
 * sao acessiveis publicamente (sem autenticacao JWT).
 * Isso e necessario para health checks de load balancers e monitoring.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.jwt.secret=test-secret-key-that-is-at-least-32-bytes-long!",
        "app.jwt.expiration-minutes=60",
        "app.admin.username=testadmin",
        "app.admin.password=testpass",
        "app.cors.allowed-origins=http://localhost:3000",
        "spring.cache.type=simple",
        "management.endpoints.web.exposure.include=health,info"
})
class ActuatorEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpoint_shouldReturn200WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void infoEndpoint_shouldReturn200WithoutAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }
}
