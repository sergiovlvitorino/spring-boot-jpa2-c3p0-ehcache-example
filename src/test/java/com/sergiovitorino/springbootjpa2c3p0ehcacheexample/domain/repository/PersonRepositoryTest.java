package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.repository;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PersonRepositoryTest {

    @Autowired
    private PersonRepository repository;

    @Test
    void save_shouldPersistPersonAndGenerateId() {
        Person person = Person.builder().name("Alice").job("Engineer").build();

        Person saved = repository.save(person);

        assertNotNull(saved.getId());
        assertEquals("Alice", saved.getName());
        assertEquals("Engineer", saved.getJob());
    }

    @Test
    void findById_afterSave_shouldReturnSamePerson() {
        Person person = Person.builder().name("Bob").job("Designer").build();
        Person saved = repository.save(person);

        Optional<Person> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("Bob", found.get().getName());
        assertEquals("Designer", found.get().getJob());
    }

    @Test
    void findById_withNonExistingId_shouldReturnEmpty() {
        Optional<Person> found = repository.findById(UUID.randomUUID());

        assertFalse(found.isPresent());
    }

    @Test
    void save_multiplePeople_shouldAssignDistinctIds() {
        Person p1 = repository.save(Person.builder().name("Carol").job("DevOps").build());
        Person p2 = repository.save(Person.builder().name("Dan").job("QA").build());

        assertNotEquals(p1.getId(), p2.getId());
    }

    @Test
    void save_shouldFlushAndMakePersonQueryable() {
        Person person = Person.builder().name("Eve").job("PM").build();
        repository.saveAndFlush(person);

        long count = repository.count();
        assertTrue(count >= 1);
    }
}
