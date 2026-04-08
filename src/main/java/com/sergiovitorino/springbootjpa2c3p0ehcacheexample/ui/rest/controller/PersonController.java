package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.controller;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person.SaveCommand;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.service.PersonService;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/person")
@RestController
@RequiredArgsConstructor
public class PersonController {

	private final PersonService personService;

	@PostMapping
	public ResponseEntity<Person> post(@RequestBody @Valid SaveCommand command) {
		Person person = Person.builder().name(command.name()).job(command.job()).build();
		return ResponseEntity.status(HttpStatus.CREATED).body(personService.save(person));
	}

	@GetMapping("{id}")
	public ResponseEntity<Person> find(@PathVariable UUID id) {
		return ResponseEntity.ok(personService.findById(id));
	}

}
