package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.controller.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
public class AddressCrudIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private Integer port;

    private String jwtToken;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeEach
    void setUp() throws Exception {
        jwtToken = getJwtToken();
    }

    private String getJwtToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String loginJson = "{\"username\":\"admin\",\"password\":\"changeme\"}";
        HttpEntity<String> entity = new HttpEntity<>(loginJson, headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl() + "/auth/login",
                HttpMethod.POST, entity,
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        return (String) response.getBody().get("token");
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(jwtToken);
        return headers;
    }

    private String createPerson() {
        String personJson = "{\"name\":\"Integration Test\",\"job\":\"Tester\"}";
        HttpEntity<String> entity = new HttpEntity<>(personJson, authHeaders());

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl() + "/api/person",
                HttpMethod.POST, entity,
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        return (String) response.getBody().get("id");
    }

    @Test
    void addressCrudFlow_shouldCreateListAndDelete() {
        // Criar Person
        String personId = createPerson();
        assertNotNull(personId);

        // Criar Address
        String addressJson = "{\"street\":\"123 Main St\",\"city\":\"Springfield\",\"state\":\"IL\",\"zipCode\":\"62701\"}";
        HttpEntity<String> createEntity = new HttpEntity<>(addressJson, authHeaders());

        ResponseEntity<Map<String, Object>> createResponse = restTemplate.exchange(
                baseUrl() + "/api/person/" + personId + "/addresses",
                HttpMethod.POST, createEntity,
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});

        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        assertNotNull(createResponse.getBody());
        String addressId = (String) createResponse.getBody().get("id");
        assertNotNull(addressId);
        assertEquals("123 Main St", createResponse.getBody().get("street"));
        assertEquals("Springfield", createResponse.getBody().get("city"));

        // Listar Addresses
        HttpHeaders getHeaders = new HttpHeaders();
        getHeaders.setBearerAuth(jwtToken);
        HttpEntity<Void> getEntity = new HttpEntity<>(getHeaders);

        ResponseEntity<Map<String, Object>> listResponse = restTemplate.exchange(
                baseUrl() + "/api/person/" + personId + "/addresses",
                HttpMethod.GET, getEntity,
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});

        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertNotNull(listResponse.getBody());
        assertNotNull(listResponse.getBody().get("content"));

        // Deletar Address
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl() + "/api/person/" + personId + "/addresses/" + addressId,
                HttpMethod.DELETE, getEntity, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, deleteResponse.getStatusCode());

        // Verificar que lista esta vazia apos deletar
        ResponseEntity<Map<String, Object>> listAfterDelete = restTemplate.exchange(
                baseUrl() + "/api/person/" + personId + "/addresses",
                HttpMethod.GET, getEntity,
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});

        assertEquals(HttpStatus.OK, listAfterDelete.getStatusCode());
        @SuppressWarnings("unchecked")
        java.util.List<?> content = (java.util.List<?>) listAfterDelete.getBody().get("content");
        assertTrue(content.isEmpty());
    }

    @Test
    void createAddress_withoutAuth_shouldReturn401() {
        String personId = createPerson();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String addressJson = "{\"street\":\"456 Oak Ave\",\"city\":\"Shelbyville\",\"state\":\"IL\",\"zipCode\":\"62702\"}";
        HttpEntity<String> entity = new HttpEntity<>(addressJson, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl() + "/api/person/" + personId + "/addresses", entity, String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void createAddress_withNonExistingPerson_shouldReturn404() {
        String fakePersonId = "00000000-0000-0000-0000-000000000000";

        String addressJson = "{\"street\":\"789 Elm Blvd\",\"city\":\"Capital City\",\"state\":\"IL\",\"zipCode\":\"62703\"}";
        HttpEntity<String> entity = new HttpEntity<>(addressJson, authHeaders());

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl() + "/api/person/" + fakePersonId + "/addresses",
                HttpMethod.POST, entity,
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteAddress_withNonExistingAddress_shouldReturn404() {
        String personId = createPerson();
        String fakeAddressId = "00000000-0000-0000-0000-000000000000";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                baseUrl() + "/api/person/" + personId + "/addresses/" + fakeAddressId,
                HttpMethod.DELETE, entity,
                new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
