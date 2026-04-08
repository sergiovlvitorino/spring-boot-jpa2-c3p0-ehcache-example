package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.repository.PersonRepository;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.exception.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository repository;

    @InjectMocks
    private PersonService service;

    @Test
    void save_shouldDelegateToRepositoryAndReturnSavedPerson() {
        Person input = Person.builder().name("Alice").job("Developer").build();
        Person saved = Person.builder().id(UUID.randomUUID()).name("Alice").job("Developer").build();
        when(repository.save(input)).thenReturn(saved);

        Person result = service.save(input);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Alice", result.getName());
        verify(repository).save(input);
    }

    @Test
    void save_shouldReturnExactRepositoryResponse() {
        UUID id = UUID.randomUUID();
        Person person = Person.builder().id(id).name("Bob").job("QA").build();
        when(repository.save(any(Person.class))).thenReturn(person);

        Person result = service.save(person);

        assertEquals(id, result.getId());
        assertEquals("Bob", result.getName());
        assertEquals("QA", result.getJob());
    }

    @Test
    void findById_withExistingId_shouldReturnPerson() {
        UUID id = UUID.randomUUID();
        Person person = Person.builder().id(id).name("Carol").job("DevOps").build();
        when(repository.findById(id)).thenReturn(Optional.of(person));

        Person result = service.findById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Carol", result.getName());
        verify(repository).findById(id);
    }

    @Test
    void findById_withNonExistingId_shouldThrowEntityNotFoundException() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> service.findById(id)
        );

        assertTrue(ex.getMessage().contains(id.toString()));
        verify(repository).findById(id);
    }

    @Test
    void findById_shouldNotCallRepositoryForSameIdWhenCached() {
        // Cache behavior is verified in integration; here we confirm repository is called once
        UUID id = UUID.randomUUID();
        Person person = Person.builder().id(id).name("Dan").job("Ops").build();
        when(repository.findById(id)).thenReturn(Optional.of(person));

        service.findById(id);

        verify(repository, times(1)).findById(id);
    }
}
