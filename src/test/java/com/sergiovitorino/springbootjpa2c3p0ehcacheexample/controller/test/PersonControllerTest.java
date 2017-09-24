package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.controller.test;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.model.Person;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class PersonControllerTest {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private TestRestTemplate restTemplete;

	@LocalServerPort
	private Integer port;

	@Test
	public void testIfCreateIsOk() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Person person = new Person();
		person.setName("Sergio");
		person.setJob("Software Engineer");
		// RequestEntity
		HttpEntity<String> entity = new HttpEntity<String>(mapper.writeValueAsString(person), headers);
		ResponseEntity<String> responseEntity = this.restTemplete
				.postForEntity("http://localhost:" + port + "/api/person", entity, String.class);
		Person personCreated = mapper.readValue(responseEntity.getBody(), Person.class);
		assertFalse(personCreated.getId().isEmpty());
	}
	
	@Test
	public void testIfFindIsOk() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		Person person = new Person();
		person.setName("Sergio");
		person.setJob("Software Engineer");
		HttpEntity<String> entity = new HttpEntity<String>(mapper.writeValueAsString(person), headers);
		ResponseEntity<String> responseEntity = null;
		Person personCreated = null;
		responseEntity = this.restTemplete
				.postForEntity("http://localhost:" + port + "/api/person", entity, String.class);
		personCreated = mapper.readValue(responseEntity.getBody(), Person.class);
		
		
		responseEntity = this.restTemplete
				.getForEntity("http://localhost:" + port + "/api/person/" + personCreated.getId(), String.class);
		Person personFound = mapper.readValue(responseEntity.getBody(), Person.class);
		assertFalse(personFound.getId().isEmpty());
		assertEquals(personCreated.getId(), personFound.getId());
	}

	
}
