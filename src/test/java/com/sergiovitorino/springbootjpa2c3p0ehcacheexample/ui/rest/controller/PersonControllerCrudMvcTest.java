package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.controller;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service.PersonService;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PersonController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.admin.username=admin",
        "app.admin.password=changeme",
        "app.jwt.secret=test-secret-key-must-be-at-least-32-chars!",
        "app.jwt.expiration-minutes=60",
        "app.cors.allowed-origins=http://localhost:3000",
        "spring.cache.type=none"
})
class PersonControllerCrudMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private JwtUtil jwtUtil;

    // --- findAll ---

    @Test
    @WithMockUser
    void findAll_shouldReturn200WithPaginatedResults() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Person person1 = Person.builder().id(id1).name("Alice").job("Engineer").build();
        Person person2 = Person.builder().id(id2).name("Bob").job("Designer").build();

        Page<Person> page = new PageImpl<>(List.of(person1, person2), PageRequest.of(0, 20), 2);
        when(personService.findAll(any(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/person")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Alice"))
                .andExpect(jsonPath("$.content[1].name").value("Bob"))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(20));
    }

    @Test
    @WithMockUser
    void findAll_withNameFilter_shouldReturn200WithFilteredResults() throws Exception {
        UUID id = UUID.randomUUID();
        Person person = Person.builder().id(id).name("Alice").job("Engineer").build();

        Page<Person> page = new PageImpl<>(List.of(person), PageRequest.of(0, 20), 1);
        when(personService.findAll(eq("Ali"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/person")
                        .param("name", "Ali"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Alice"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void findAll_withoutAuth_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/person"))
                .andExpect(status().isUnauthorized());
    }

    // --- update ---

    @Test
    @WithMockUser
    void update_withValidCommand_shouldReturn200WithUpdatedPerson() throws Exception {
        UUID id = UUID.randomUUID();
        Person updated = Person.builder().id(id).name("Alice Updated").job("Senior Engineer").build();
        when(personService.update(eq(id), any(Person.class))).thenReturn(updated);

        mockMvc.perform(put("/api/person/{id}", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice Updated\",\"job\":\"Senior Engineer\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Updated"))
                .andExpect(jsonPath("$.job").value("Senior Engineer"));
    }

    @Test
    @WithMockUser
    void update_withNonExistingId_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(personService.update(eq(id), any(Person.class)))
                .thenThrow(new EntityNotFoundException("Person not found with id: " + id));

        mockMvc.perform(put("/api/person/{id}", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"job\":\"Engineer\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser
    void update_withBlankName_shouldReturn400() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/api/person/{id}", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"job\":\"Engineer\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fields.name").exists());
    }

    @Test
    void update_withoutAuth_shouldReturn401() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/api/person/{id}", id.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Alice\",\"job\":\"Engineer\"}"))
                .andExpect(status().isUnauthorized());
    }

    // --- delete ---

    @Test
    @WithMockUser
    void delete_withExistingId_shouldReturn204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(personService).delete(id);

        mockMvc.perform(delete("/api/person/{id}", id.toString()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void delete_withNonExistingId_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new EntityNotFoundException("Person not found with id: " + id))
                .when(personService).delete(id);

        mockMvc.perform(delete("/api/person/{id}", id.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void delete_withoutAuth_shouldReturn401() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/person/{id}", id.toString()))
                .andExpect(status().isUnauthorized());
    }
}
