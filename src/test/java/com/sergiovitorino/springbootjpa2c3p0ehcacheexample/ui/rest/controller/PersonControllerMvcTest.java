package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.controller;

import tools.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person.SaveCommand;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service.PersonService;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.exception.EntityNotFoundException;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security.JwtUtil;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
class PersonControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PersonService personService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    @WithMockUser
    void post_withValidCommand_shouldReturn201WithPersonBody() throws Exception {
        SaveCommand command = new SaveCommand();
        command.setName("Alice");
        command.setJob("Engineer");

        UUID id = UUID.randomUUID();
        Person saved = Person.builder().id(id).name("Alice").job("Engineer").build();
        when(personService.save(any(Person.class))).thenReturn(saved);

        mockMvc.perform(post("/api/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Alice"))
                .andExpect(jsonPath("$.job").value("Engineer"));
    }

    @Test
    @WithMockUser
    void post_withBlankName_shouldReturn400() throws Exception {
        SaveCommand command = new SaveCommand();
        command.setName("");
        command.setJob("Engineer");

        mockMvc.perform(post("/api/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fields.name").exists());
    }

    @Test
    @WithMockUser
    void post_withBlankJob_shouldReturn400() throws Exception {
        SaveCommand command = new SaveCommand();
        command.setName("Alice");
        command.setJob("   ");

        mockMvc.perform(post("/api/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.job").exists());
    }

    @Test
    @WithMockUser
    void find_withExistingId_shouldReturn200WithPersonBody() throws Exception {
        UUID id = UUID.randomUUID();
        Person person = Person.builder().id(id).name("Bob").job("DevOps").build();
        when(personService.findById(id)).thenReturn(person);

        mockMvc.perform(get("/api/person/{id}", id.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Bob"));
    }

    @Test
    @WithMockUser
    void find_withNonExistingId_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(personService.findById(id))
                .thenThrow(new EntityNotFoundException("Person not found with id: " + id));

        mockMvc.perform(get("/api/person/{id}", id.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void post_withoutAuthentication_shouldReturn401() throws Exception {
        SaveCommand command = new SaveCommand();
        command.setName("Alice");
        command.setJob("Engineer");

        mockMvc.perform(post("/api/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isUnauthorized());
    }
}
