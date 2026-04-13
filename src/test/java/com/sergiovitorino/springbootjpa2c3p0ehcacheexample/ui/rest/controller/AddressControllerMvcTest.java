package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.controller;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service.AddressService;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Address;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.exception.EntityNotFoundException;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security.JwtUtil;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AddressController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.admin.username=admin",
        "app.admin.password=changeme",
        "app.jwt.secret=test-secret-key-must-be-at-least-32-chars!",
        "app.jwt.expiration-minutes=60",
        "app.cors.allowed-origins=http://localhost:3000",
        "spring.cache.type=none"
})
class AddressControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AddressService addressService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser
    void create_withValidCommand_shouldReturn201() throws Exception {
        UUID personId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();
        Address saved = Address.builder()
                .id(addressId)
                .street("123 Main St")
                .city("Springfield")
                .state("IL")
                .zipCode("62701")
                .build();

        when(addressService.create(eq(personId), any(Address.class))).thenReturn(saved);

        mockMvc.perform(post("/api/person/{personId}/addresses", personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"street\":\"123 Main St\",\"city\":\"Springfield\",\"state\":\"IL\",\"zipCode\":\"62701\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(addressId.toString()))
                .andExpect(jsonPath("$.street").value("123 Main St"))
                .andExpect(jsonPath("$.city").value("Springfield"))
                .andExpect(jsonPath("$.state").value("IL"))
                .andExpect(jsonPath("$.zipCode").value("62701"));
    }

    @Test
    @WithMockUser
    void create_withBlankStreet_shouldReturn400() throws Exception {
        UUID personId = UUID.randomUUID();

        mockMvc.perform(post("/api/person/{personId}/addresses", personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"street\":\"\",\"city\":\"Springfield\",\"state\":\"IL\",\"zipCode\":\"62701\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fields.street").exists());
    }

    @Test
    @WithMockUser
    void create_withNonExistingPerson_shouldReturn404() throws Exception {
        UUID personId = UUID.randomUUID();

        when(addressService.create(eq(personId), any(Address.class)))
                .thenThrow(new EntityNotFoundException("Person not found with id: " + personId));

        mockMvc.perform(post("/api/person/{personId}/addresses", personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"street\":\"123 Main St\",\"city\":\"Springfield\",\"state\":\"IL\",\"zipCode\":\"62701\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void create_withoutAuth_shouldReturn401() throws Exception {
        UUID personId = UUID.randomUUID();

        mockMvc.perform(post("/api/person/{personId}/addresses", personId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"street\":\"123 Main St\",\"city\":\"Springfield\",\"state\":\"IL\",\"zipCode\":\"62701\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void findByPersonId_shouldReturn200WithPaginatedResults() throws Exception {
        UUID personId = UUID.randomUUID();
        Address addr = Address.builder()
                .id(UUID.randomUUID())
                .street("123 Main St")
                .city("Springfield")
                .state("IL")
                .zipCode("62701")
                .build();
        Page<Address> page = new PageImpl<>(List.of(addr), PageRequest.of(0, 20), 1);

        when(addressService.findByPersonId(eq(personId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/person/{personId}/addresses", personId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].street").value("123 Main St"))
                .andExpect(jsonPath("$.content[0].city").value("Springfield"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void findByPersonId_withoutAuth_shouldReturn401() throws Exception {
        UUID personId = UUID.randomUUID();

        mockMvc.perform(get("/api/person/{personId}/addresses", personId))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void delete_withExistingAddress_shouldReturn204() throws Exception {
        UUID personId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        doNothing().when(addressService).delete(personId, addressId);

        mockMvc.perform(delete("/api/person/{personId}/addresses/{addressId}", personId, addressId))
                .andExpect(status().isNoContent());

        verify(addressService).delete(personId, addressId);
    }

    @Test
    @WithMockUser
    void delete_withNonExistingAddress_shouldReturn404() throws Exception {
        UUID personId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        doThrow(new EntityNotFoundException("Address not found with id: " + addressId))
                .when(addressService).delete(personId, addressId);

        mockMvc.perform(delete("/api/person/{personId}/addresses/{addressId}", personId, addressId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void delete_withoutAuth_shouldReturn401() throws Exception {
        UUID personId = UUID.randomUUID();
        UUID addressId = UUID.randomUUID();

        mockMvc.perform(delete("/api/person/{personId}/addresses/{addressId}", personId, addressId))
                .andExpect(status().isUnauthorized());
    }
}
