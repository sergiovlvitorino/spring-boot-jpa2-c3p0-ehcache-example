package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PersonService {

    @Autowired private PersonRepository repository;

    public Person save(Person person) {
        return repository.save(person);
    }

    public Person findById(UUID id) {
        return repository.findById(id).orElseThrow(()-> new IllegalArgumentException("Person not found"));
    }
}
