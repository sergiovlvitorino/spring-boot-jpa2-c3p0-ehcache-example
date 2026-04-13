package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.controller;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person.SaveCommand;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person.UpdateCommand;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service.PersonService;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.dto.PersonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/person")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PostMapping
    public ResponseEntity<PersonResponse> post(@RequestBody @Valid SaveCommand command) {
        Person person = Person.builder().name(command.name()).job(command.job()).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(PersonResponse.from(personService.save(person)));
    }

    @GetMapping("{id}")
    public ResponseEntity<PersonResponse> find(@PathVariable UUID id) {
        return ResponseEntity.ok(PersonResponse.from(personService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<Page<PersonResponse>> findAll(
            @RequestParam(required = false) String name,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<PersonResponse> page = personService.findAll(name, pageable)
                .map(PersonResponse::from);
        return ResponseEntity.ok(page);
    }

    @PutMapping("{id}")
    public ResponseEntity<PersonResponse> update(@PathVariable UUID id,
                                                  @RequestBody @Valid UpdateCommand command) {
        Person updated = Person.builder().name(command.name()).job(command.job()).build();
        return ResponseEntity.ok(PersonResponse.from(personService.update(id, updated)));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        personService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
