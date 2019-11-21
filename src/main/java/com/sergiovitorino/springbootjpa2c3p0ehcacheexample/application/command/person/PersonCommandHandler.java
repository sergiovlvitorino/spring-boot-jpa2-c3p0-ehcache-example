package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service.PersonService;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class PersonCommandHandler {

    @Autowired private PersonService service;

    public Person execute(SaveCommand command){
        Person person = Person.builder().name(command.getName()).job(command.getJob()).build();
        return service.save(person);
    }

    public Person execute(FindByIdCommand command) {
        return service.findById(command.getId());
    }
}
