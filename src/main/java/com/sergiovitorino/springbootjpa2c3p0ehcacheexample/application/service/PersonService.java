package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.repository.PersonRepository;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.exception.EntityNotFoundException;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonService {

    private static final Logger log = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository repository;
    private final MeterRegistry meterRegistry;

    @Transactional
    @CachePut(value = "personCache", key = "#result.id")
    public Person save(Person person) {
        Person saved = repository.save(person);
        log.info("Person created with id: {}", saved.getId());
        meterRegistry.counter("person.created").increment();
        return saved;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "personCache", key = "#id")
    public Person findById(UUID id) {
        log.debug("Finding person by id: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Person> findAll(String name, Pageable pageable) {
        if (name != null && !name.isBlank()) {
            return repository.findByNameContainingIgnoreCase(name, pageable);
        }
        return repository.findAll(pageable);
    }

    @Transactional
    @CachePut(value = "personCache", key = "#id")
    public Person update(UUID id, Person updated) {
        Person person = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with id: " + id));
        person.setName(updated.getName());
        person.setJob(updated.getJob());
        log.info("Person updated: {}", id);
        return person;
    }

    @Transactional
    @CacheEvict(value = "personCache", key = "#id")
    public void delete(UUID id) {
        Person person = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with id: " + id));
        repository.delete(person);
        log.info("Person deleted: {}", id);
        meterRegistry.counter("person.deleted").increment();
    }
}
