package com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.repository;

import com.sergiovitorino.springbootjpa2c3p0ehcacheexample.domain.model.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> {

}
