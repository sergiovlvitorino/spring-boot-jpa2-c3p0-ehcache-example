package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.controller;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person.FindByIdCommand;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person.PersonCommandHandler;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person.SaveCommand;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/person")
@RestController
@Validated
public class PersonController {

	@Autowired
	private PersonCommandHandler commandHandler;

	@PostMapping
	public ResponseEntity<Person> post(@RequestBody @Valid SaveCommand command) {
		return ResponseEntity.status(HttpStatus.CREATED).body(commandHandler.execute(command));
	}

	@GetMapping("{id}")
	public ResponseEntity<Person> find(@Valid FindByIdCommand command) {
		return ResponseEntity.ok(commandHandler.execute(command));
	}

}
