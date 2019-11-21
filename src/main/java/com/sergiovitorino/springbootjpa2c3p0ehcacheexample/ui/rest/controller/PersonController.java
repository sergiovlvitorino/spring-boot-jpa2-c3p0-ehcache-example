package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.ui.rest.controller;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person.FindByIdCommand;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person.PersonCommandHandler;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person.SaveCommand;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("/api/person")
@RestController
@Validated
public class PersonController {

	@Autowired private PersonCommandHandler commandHandler;
	
	@PostMapping
	public Person post(@RequestBody @Valid SaveCommand command){
		return commandHandler.execute(command);
	}
	
	@GetMapping("{id}")
	public Person find(@Valid FindByIdCommand command){
		return commandHandler.execute(command);
	}
	
}
