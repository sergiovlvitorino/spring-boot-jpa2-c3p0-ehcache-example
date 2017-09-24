package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.core;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.dao.PersonDao;
import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.model.Person;

@Component
public class PersonCore {

	@Autowired
	private PersonDao dao;
	
	public Person find(String id) {
		return dao.getObjectById(id);
	}

	public Person create(Person person) {
		person.setId(UUID.randomUUID().toString());
		dao.persist(person);
		return person;
	}
	
}
