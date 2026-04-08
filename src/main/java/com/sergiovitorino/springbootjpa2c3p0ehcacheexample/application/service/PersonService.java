package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.repository.PersonRepository;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.infrastructure.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PersonService {

    private static final Logger log = LoggerFactory.getLogger(PersonService.class);

    private final PersonRepository repository;

    @Transactional
    @CachePut(value = "personCache", key = "#result.id")
    public Person save(Person person) {
        Person saved = repository.save(person);
        log.info("Person created with id: {}", saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "personCache", key = "#id")
    public Person findById(UUID id) {
        log.debug("Finding person by id: {}", id);
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Person not found with id: " + id));
    }

}
