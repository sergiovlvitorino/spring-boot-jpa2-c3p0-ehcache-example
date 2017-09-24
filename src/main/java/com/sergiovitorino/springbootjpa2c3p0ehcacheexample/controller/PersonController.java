package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.core.PersonCore;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.model.Person;

@RequestMapping("/api")
@RestController
public class PersonController {

	@Autowired
	private PersonCore core;
	
	@Autowired
	private ObjectMapper mapper;
	
	@RequestMapping(value = "person" , method = RequestMethod.POST)
	public ResponseEntity<String> create(@RequestBody Person person){
		try {
			return ResponseEntity.status(HttpStatus.CREATED).body(mapper.writeValueAsString(core.create(person)));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
	
	@RequestMapping(value = "person/{id}" , method = RequestMethod.GET)
	public ResponseEntity<String> find(@PathVariable(required = true) String id ){
		try {
			return ResponseEntity.ok(mapper.writeValueAsString(core.find(id)));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
	
}
