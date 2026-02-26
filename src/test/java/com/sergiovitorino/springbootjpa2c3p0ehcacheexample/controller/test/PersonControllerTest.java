package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.controller.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.auth.LoginCommand;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.application.command.person.SaveCommand;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class PersonControllerTest {

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private TestRestTemplate restTemplate;

	@LocalServerPort
	private Integer port;

	private String getJwtToken() throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		LoginCommand login = new LoginCommand();
		login.setUsername("admin");
		login.setPassword("changeme");

		HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(login), headers);
		ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
				"http://localhost:" + port + "/auth/login",
				HttpMethod.POST, entity,
				new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {});

		assertEquals(HttpStatus.OK, response.getStatusCode());
		return (String) response.getBody().get("token");
	}

	@Test
	public void testIfCreateIsOk() throws Exception {
		String token = getJwtToken();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token);

		SaveCommand command = new SaveCommand();
		command.setName("Sergio");
		command.setJob("Software Engineer");

		HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/person", entity, String.class);

		assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
		Person personCreated = mapper.readValue(responseEntity.getBody(), Person.class);
		assertNotNull(personCreated);
		assertNotNull(personCreated.getId());
	}

	@Test
	public void testIfFindIsOk() throws Exception {
		String token = getJwtToken();

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.setBearerAuth(token);

		SaveCommand command = new SaveCommand();
		command.setName("Sergio");
		command.setJob("Software Engineer");

		HttpEntity<String> entity = new HttpEntity<>(mapper.writeValueAsString(command), headers);
		ResponseEntity<String> responseEntity = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/person", entity, String.class);

		Person personCreated = mapper.readValue(responseEntity.getBody(), Person.class);

		HttpHeaders getHeaders = new HttpHeaders();
		getHeaders.setBearerAuth(token);
		HttpEntity<Void> getEntity = new HttpEntity<>(getHeaders);

		ResponseEntity<String> getResponse = restTemplate.exchange(
				"http://localhost:" + port + "/api/person/" + personCreated.getId(),
				HttpMethod.GET, getEntity, String.class);

		assertEquals(HttpStatus.OK, getResponse.getStatusCode());
		Person personFound = mapper.readValue(getResponse.getBody(), Person.class);
		assertNotNull(personFound);
		assertEquals(personCreated.getId(), personFound.getId());
	}

	@Test
	public void testUnauthorizedWithoutToken() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		ResponseEntity<String> response = restTemplate.postForEntity(
				"http://localhost:" + port + "/api/person",
				new HttpEntity<>("{\"name\":\"test\",\"job\":\"test\"}", headers),
				String.class);

		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
	}

}
